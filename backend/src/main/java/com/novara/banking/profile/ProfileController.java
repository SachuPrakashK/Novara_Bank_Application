package com.novara.banking.profile;
import com.novara.banking.identity.CurrentIdentity;import jakarta.validation.Valid;import org.springframework.security.core.Authentication;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/profile") public class ProfileController{
 private final ProfileService service;private final CurrentIdentity identities;public ProfileController(ProfileService s,CurrentIdentity i){service=s;identities=i;}
 @GetMapping ProfileService.Profile profile(Authentication a){return service.view(identities.from(a));}
 @PatchMapping ProfileService.Profile update(Authentication a,@Valid @RequestBody ProfileService.ProfileUpdate r){return service.update(identities.from(a),r);}
 @GetMapping("/preferences") ProfileService.Preferences preferences(Authentication a){return service.preferences(identities.from(a));}
 @PutMapping("/preferences") ProfileService.Preferences updatePreferences(Authentication a,@Valid @RequestBody ProfileService.Preferences r){return service.updatePreferences(identities.from(a),r);}
}
