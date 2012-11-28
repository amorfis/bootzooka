package pl.softwaremill.bootstrap.service.user

import org.specs2.mutable.Specification
import pl.softwaremill.bootstrap.dao.UserDAO
import org.specs2.mock.Mockito
import pl.softwaremill.bootstrap.domain.User

class UserServiceSpec extends Specification with Mockito {

  def prepareUserDAOMock: UserDAO = {
    val dao = mock[UserDAO]
    dao.findByEmail("admin@sml.com") returns Some(new User("admin", "admin@sml.com", "pass"))
    dao.findByEmail("newUser@sml.com") returns None
    dao.findByLogin("admin") returns Some(new User("admin", "admin@sml.com", "pass"))
    dao.findByLogin("newUser") returns None
    dao
  }

  val registrationDataValidator: RegistrationDataValidator = mock[RegistrationDataValidator]
  val userService = new UserService(prepareUserDAOMock, registrationDataValidator)

  "findByEmail" should { // this test is silly :\
    "return user for admin@sml.pl" in {


      val user: User = userService.findByEmail("admin@sml.com").getOrElse(null)

      there was user !== null
      there was user.login === "admin"
      there was user.email === "admin@sml.com"
    }
  }

  "checkExistence" should {
    val userService = new UserService(prepareUserDAOMock, registrationDataValidator)

    "don't find given user login and e-mail" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "newUser@sml.com")
      userExistence.isRight === true
    }

    "find duplicated login" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("admin", "newUser@sml.com")

      userExistence.isLeft === true
      userExistence.left.get.equals("Login already in use!")
    }

    "find duplicated email" in {
      val userExistence: Either[String, Unit] = userService.checkUserExistenceFor("newUser", "admin@sml.com")

      userExistence.isLeft === true
      userExistence.left.get.equals("E-mail already in use!")
    }
  }

}
