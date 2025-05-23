package ma.fs.hopital.security.repo;

import ma.fs.hopital.security.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    AppUser findByUsername(String username);


}
