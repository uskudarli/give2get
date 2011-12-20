import play.jobs.OnApplicationStart;
import play.jobs.Job;
import com.boun.give2get.mail.MailFactory;
import com.boun.give2get.db.DAO;
import com.boun.give2get.db.SearchDAO;
import com.boun.give2get.core.Messages;
import com.boun.give2get.core.Cache;
import org.apache.log4j.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 4:55:05 PM
 * To change this template use File | Settings | File Templates.
 */
@OnApplicationStart
public class Bootstrap extends Job {

    private static final Logger log = Logger.getLogger(Bootstrap.class);

    public void doJob() throws Exception {

        DAO.init();
        log.info("DAO initialized!");


        MailFactory.init();
        log.info("Mail Factory initialized!");


        Messages.init();
        log.info("Messages initialized!");


        Cache.setSehirler(SearchDAO.getSehirler());
        log.info("Cities initialized!");

        
        log.info("Give2Get Started!");
        System.out.println("Give2Get Started!");

    }
}
