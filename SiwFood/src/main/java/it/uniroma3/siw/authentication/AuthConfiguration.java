package it.uniroma3.siw.authentication;

import org.springframework.context.annotation.Configuration; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

import static it.uniroma3.siw.model.Credentials.ADMIN_ROLE;
import static it.uniroma3.siw.model.Credentials.DEFAULT_ROLE;

@Configuration
@EnableWebSecurity
public class AuthConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource datasource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            // Permette l'accesso senza autenticazione a queste risorse
            .antMatchers(HttpMethod.GET, "/public/**", "/public/**/*.html", "/", "/index", "/register", "/css/**", "/images/**", "/favicon.ico").permitAll()
            // Permette l'accesso senza autenticazione alla registrazione
            .antMatchers(HttpMethod.POST, "/public/register").permitAll()
            // Autorizza l'accesso agli endpoint admin solo agli utenti con ruolo ADMIN_ROLE
         // Accesso agli endpoint admin solo agli utenti con ruolo ADMIN_ROLE
            .antMatchers("/admin/**", "/admin/**/*.html").hasAuthority(ADMIN_ROLE)
            // Accesso agli endpoint chef sia per utenti con DEFAULT_ROLE che ADMIN_ROLE
            .antMatchers("/chef/**", "/chef/**/*.html").hasAnyAuthority(DEFAULT_ROLE, ADMIN_ROLE)
            // Per tutti gli altri endpoint richiede l'autenticazione
            .anyRequest().authenticated()
            // Configura la pagina di accesso negato
            .and().exceptionHandling().accessDeniedPage("/index")
            // Configura il form di login
            .and().formLogin().loginPage("/public/login").defaultSuccessUrl("/", true)
            // Configura il logout
            .and().logout().logoutUrl("/public/logout").logoutSuccessUrl("/").invalidateHttpSession(true)
            .deleteCookies("JSESSIONID").logoutRequestMatcher(new AntPathRequestMatcher("/public/logout")).clearAuthentication(true).permitAll();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
            .dataSource(this.datasource)
            .authoritiesByUsernameQuery("SELECT username, role FROM credentials WHERE username=?")
            .usersByUsernameQuery("SELECT username, password, 1 as enabled FROM credentials WHERE username=?")
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
