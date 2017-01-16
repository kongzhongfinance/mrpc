package junicorn.mrpc.demo.api;

import junicorn.mrpc.demo.model.User;

import java.util.List;

public interface UserService {

    List<User> getUsers(long uid);

    void saveUser(User user);
}
