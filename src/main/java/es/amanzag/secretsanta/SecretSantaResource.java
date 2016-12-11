package es.amanzag.secretsanta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/secret-santa")
public class SecretSantaResource {
    
    @Autowired UserRepository userRepo;
    
    @Autowired GameRepository gameRepo;
    
    @Autowired PhotoStorageService photoStorageService;
    
    @Autowired EmailNotificationService notificationService;
    
    @GetMapping
    public List<Game> getGames() {
        return gameRepo.findAll();
    }
    
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createGame(@PathVariable("id") String id, @RequestBody Game game) {
        game.setId(id);
        gameRepo.save(game);
    }
    
    @GetMapping("/{gameName}/users")
    public @ResponseBody List<User> getUsers(@PathVariable("gameName") String gameGame) {
        return Optional
            .ofNullable(gameRepo.findOne(gameGame))
            .map(game -> game.getUsers())
            .orElse(Collections.emptyList());
    }
    
    @PutMapping(path="/{gameName}/users/{userId}", consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createUser(
            @PathVariable("gameName") String gameGame, 
            @PathVariable("userId") String userId,
            @RequestBody User user) {
        Game game = gameRepo.findOne(gameGame);
        if(game == null) {
            return ResponseEntity.notFound().build();
        }
        user.setId(userId);
        user.setToken(UUID.randomUUID());
        user.setGame(game);
        userRepo.save(user);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping(path="/{gameId}/users/{userId}/photo")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadPicture(
            @PathVariable("gameId") String gameId, 
            @PathVariable("userId") String userId, 
            @RequestParam("file") MultipartFile file) throws IOException {
        User u = Optional.ofNullable(userRepo.findOne(userId))
                .filter(user -> user.getGame().getId().equals(gameId))
                .orElseThrow(() -> new NotFoundException());
        photoStorageService.savePicture(u, file.getInputStream());
    }
    
    @GetMapping(path="/{gameId}/users/{userId}/photo", produces=MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody InputStreamResource getUserPhoto(
            @PathVariable("gameId") String gameId, 
            @PathVariable("userId") String userId) throws IOException {
        User u = Optional.ofNullable(userRepo.findOne(userId))
                .filter(user -> user.getGame().getId().equals(gameId))
                .orElseThrow(() -> new NotFoundException());
        return new InputStreamResource(photoStorageService.getPicture(u));
    }
        
    
    @GetMapping(path="/{gameName}/users/{userName}", produces=MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody User getUser(
            @PathVariable("gameName") String gameName, 
            @PathVariable("userName") String userName,
            @RequestParam(value="token", required=true) String token) {
         User user = Optional.ofNullable(userRepo.findOne(userName))
                 .filter(u -> u.getGame().getId().equals(gameName))
                 .orElseThrow(() -> new NotFoundException());
         if (!user.getToken().toString().equals(token)) {
             throw new UnauthorizedException();
         }
         return user;
    }
    
    @GetMapping(path="/{gameName}/users/{userName}", produces=MediaType.TEXT_HTML_VALUE)
    public ModelAndView getUserPage(
            @PathVariable("gameName") String gameGame, 
            @PathVariable("userName") String userName,
            @RequestParam(value="token", required=true) String token) {
        ModelAndView mav = new ModelAndView("user");
        mav.addObject("user", getUser(gameGame, userName, token));
        return mav;
    }
    
    @PostMapping("/{gameName}/draw") @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void draw(@PathVariable("gameName") String gameName) {
        Optional
                .ofNullable(gameRepo.findOne(gameName))
                .map(game -> game.getUsers())
                .ifPresent(users -> assignNext(shuffle(users)));
    }
    
    @PostMapping("/{gameName}/notify-users") @ResponseStatus(HttpStatus.OK)
    public void notifyUsers(@PathVariable("gameName") String gameName) throws AddressException, MessagingException {
        List<User> users = Optional.ofNullable(gameRepo.findOne(gameName))
                .map(game -> game.getUsers())
                .orElseThrow(() -> new NotFoundException());
        
        for(User u : users) {
            notificationService.notify(u);
        }
    }
    
    private List<User> shuffle(List<User> users) {
        ArrayList<User> copy = new ArrayList<>(users);
        Collections.shuffle(copy);
        return copy;
    }
    
    private List<User> assignNext(List<User> users) {
        for(int i=0; i< users.size()-1; i++) {
            users.get(i).setGiftReceiver(users.get(i+1));
        }
        users.get(users.size()-1).setGiftReceiver(users.get(0));
        return users;
    }

}
