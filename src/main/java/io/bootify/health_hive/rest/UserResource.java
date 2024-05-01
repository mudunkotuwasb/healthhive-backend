package io.bootify.health_hive.rest;

import io.bootify.health_hive.model.UserLoginDTO;
import io.bootify.health_hive.domain.User;
import io.bootify.health_hive.model.UserDTO;
import io.bootify.health_hive.repos.UserRepository;
import io.bootify.health_hive.service.KeycloackService;
import io.bootify.health_hive.service.UserService;
import io.bootify.health_hive.util.NotFoundException;
import io.bootify.health_hive.util.ReferencedException;
import io.bootify.health_hive.util.ReferencedWarning;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserResource {

    private final UserService userService;
    private final  KeycloackService keycloakService;
    private final UserRepository userRepository;


    public UserResource(final UserService userService, final KeycloackService keycloakService, final UserRepository userRepository) {
        this.userService = userService;
        this.keycloakService = keycloakService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(userService.get(id));
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable(name = "email") final String email) {
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new NotFoundException();
        }
        return ResponseEntity.ok(userService.get(user.getId()));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createUser(@RequestBody @Valid final UserDTO userDTO) {
        final Long createdId = userService.create(userDTO);
        final String KeycloakUser = keycloakService.createUserInKeycloak(userDTO);
        System.out.println(KeycloakUser);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?>  login(@RequestBody UserLoginDTO userLoginDTO) {

        return null;
    }
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long id, @RequestBody String tempPassword) {
//        UserDTO userDTO = new UserDTO();
//        userDTO.setId(id);
//        keycloakService.resetUserPassword(userDTO, tempPassword);
//       return ResponseEntity.ok().build();
        return userService.resetPassword(id, tempPassword);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateUser(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final UserDTO userDTO) {
        userService.update(id, userDTO);
        return ResponseEntity.ok(id);

    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") final Long id) {
        final ReferencedWarning referencedWarning = userService.getReferencedWarning(id);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
