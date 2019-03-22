
package ftn.securexml.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ftn.securexml.model.Authority;
import ftn.securexml.model.User;
import ftn.securexml.repository.UserRepository;
import ftn.securexml.security.TokenUtils;
import ftn.securexml.service.CustomUserDetailsService;

@Controller
public class MainController {
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    TokenUtils tokenUtils;
    @Autowired UserRepository userRepository;

	@GetMapping(value = "/")
    public ModelAndView method(HttpServletRequest request,@RequestParam(required=false) String token) {
		
		if (token == null || token.equals(""))
			return new ModelAndView("redirect:" + "index.html");
		System.out.println(token);
		String uname = this.tokenUtils.getUsernameFromToken(token);
		User user = (User) this.userDetailsService.loadUserByUsername(uname);
        if (user == null)
            return new ModelAndView("redirect:" + "index.html");
 
        if (user != null) {
            List<Authority> li = (List<Authority>) user.getAuthorities();
            if (li.get(0).getName().equals("ROLE_SYSTEM_ADMIN"))
            	return new ModelAndView("redirect:" + "showAllCertificates.html");
            
          /*  System.out.println(li.get(0).getName() + " <-----------");
            if (li.get(0).getName().equals("ROLE_RENT_A_CAR_ADMIN"))
                return new ModelAndView("redirect:" + "racadmin.html");*/
           
/*
            if (li.get(0).getName().equals("ROLE_USER"))
            	return new ModelAndView("redirect:" + "user.html");

            // dodata stranica za administratora hotela, Marko
            if (li.get(0).getName().equals("ROLE_HOTEL_ADMIN"))
            	return new ModelAndView("redirect:" + "hoteladmin.html");
*/
        }
        return new ModelAndView("redirect:" + "index.html");
	}
	
	@GetMapping("/checkIsAdmin")
	public ResponseEntity<?> checkIsAdmin(HttpServletRequest request)
	{
		String token=tokenUtils.getToken(request);
		String username=tokenUtils.getUsernameFromToken(token);
		User user = userRepository.findOneByUsername(username);
		List<String>retVal=new ArrayList<String>();
		if(user==null)
			return ResponseEntity.ok(retVal);
		for(int i=0;i<user.getAllAuthorities().size();i++) {
			retVal.add(user.getAllAuthorities().get(i).getName());
		}
		return ResponseEntity.ok(retVal);
	}

}

