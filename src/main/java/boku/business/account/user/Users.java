package boku.business.account.user;

import boku.infra.persistance.BasicRepository;
import com.google.inject.Singleton;


@Singleton
public class Users extends BasicRepository<User.UserId, User> {

}
