import com.n33.demo.service.ServiceTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = {"classpath*:application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class AopTest {
    @Autowired
    ServiceTest serviceTest;

    @Autowired
    ApplicationContext app;

    @Test
    public void test() {

        ServiceTest aspect = app.getBean(ServiceTest.class);
        System.out.println(aspect);

        serviceTest.doIt();

        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        try {
            serviceTest.doItIdInt(1);
        } catch (RuntimeException e) {
            System.out.println(e);
        }

        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        serviceTest.doItIdInteger(1);

    }
}
