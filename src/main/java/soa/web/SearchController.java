package soa.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class SearchController {

  private final ProducerTemplate producerTemplate;

  @Autowired
  public SearchController(ProducerTemplate producerTemplate) {
    this.producerTemplate = producerTemplate;
  }

  @RequestMapping("/")
  public String index() {
    return "index";
  }


  @RequestMapping(value = "/search")
  @ResponseBody
  public Object search(@RequestParam("q") String q, 
      @RequestParam(name = "max", defaultValue = "10", required = false) int max) {
    HashMap<String, Object> headers = new HashMap<>();
    headers.put("CamelTwitterKeywords", q);
    headers.put("CamelTwitterCount", max);
    return producerTemplate.requestBodyAndHeaders("direct:search", "", headers);
  }

  private String parseParam(String arg, String prefix, String delimiter) {
    String[] _arg = arg.split("\\s+");
      List<String> hashtagList = Arrays.asList(_arg);
      if (prefix != null) {
        hashtagList = hashtagList.stream()
          .map((String s) -> {return (s.startsWith(prefix)) ? s : prefix + s;})
          .collect(Collectors.toList());
      }
      return '(' + String.join(" " + delimiter + " ", hashtagList) + ") ";
  }

  @RequestMapping(value = "/advanced")
  @ResponseBody
  public Object advanced(@RequestParam(name = "all", required = false) String all, 
      @RequestParam(name = "phrase", required = false) String phrase, 
      @RequestParam(name = "any", required = false) String any, 
      @RequestParam(name = "not", required = false) String not, 
      @RequestParam(name = "hashtag", required = false) String hashtag,
      @RequestParam(name = "from", required = false) String from, 
      @RequestParam(name = "to", required = false) String to, 
      @RequestParam(name = "at", required = false) String at, 
      @RequestParam(name = "lang", required = false) String lang, 
      @RequestParam(name = "since", required = false) String since, 
      @RequestParam(name = "until", required = false) String until, 
      @RequestParam(name = "min_replies", required = false) Integer min_replies, 
      @RequestParam(name = "min_faves", required = false) Integer min_faves, 
      @RequestParam(name = "min_retweets", required = false) Integer min_retweets, 
      @RequestParam(name = "links", required = false) Boolean links, 
      @RequestParam(name = "replies", required = false) Boolean replies, 
      @RequestParam(name = "max", defaultValue = "10", required = false) int max) {
    Boolean at_least_one = false;
    String q = "";
    if (all != null) {
      at_least_one = true;
      q += all + ' ';
    }
    if (phrase != null) {
      at_least_one = true;
      q += '\"' + phrase + "\" ";
    }
    if (any != null) {
      at_least_one = true;
      String[] _any = any.split("\\s+");
      q += '(' + String.join(" OR ", _any) + ") ";
    }
    if (not != null) {
      String[] _not = any.split("\\s+");
      q += '-' + String.join(" -", _not) + ' ';
    }
    if (hashtag != null) {
      at_least_one = true;
      q += parseParam(hashtag, "#", "OR");
    }
    if (from != null) {
      at_least_one = true;
      q += parseParam(from, "from:", "OR");
    }
    if (to != null) {
      at_least_one = true;
      q += parseParam(to, "to:", "OR");
    }
    if (at != null) {
      at_least_one = true;
      q += parseParam(at, "@", "OR");
    }
    if (lang != null) {
      q += "lang:" + lang + ' ';
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    if (since != null) {
      try
	    {
	        sdf.parse(since); 
          q += "since:" + since + ' ';
	    }
	    catch (ParseException e)
	    {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	    }
    }
    if (until != null) {
      try
      {
        sdf.parse(until); 
        q += "until:" + until + ' ';
      }
      catch (ParseException e)
      {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    }
    if (min_replies != null) {
      q += "min_replies:" + min_replies + ' ';
    }
    if (min_faves != null) {
      q += "min_faves:" + min_faves + ' ';
    }
    if (min_retweets != null) {
      q += "min_retweets:" + min_retweets + ' ';
    }
    if (links != null) {
      if (links) {
        q += "filter:links ";
      } else {
        q += "-filter:links ";
      }
    }
    if (replies != null) {
      if (replies) {
        q += "filter:replies ";
      } else {
        q += "-filter:replies ";
      }
    }
    System.out.println(q);

    if (!at_least_one) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    
    HashMap<String, Object> headers = new HashMap<>();
    headers.put("CamelTwitterKeywords", q);
    headers.put("CamelTwitterCount", max);
    return producerTemplate.requestBodyAndHeaders("direct:search", "", headers);
  }
}