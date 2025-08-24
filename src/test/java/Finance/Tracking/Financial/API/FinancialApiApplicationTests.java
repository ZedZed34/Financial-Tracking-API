package Finance.Tracking.Financial.API;

import com.zz.fintrack.FintrackApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FintrackApplication.class)
@ActiveProfiles("test")
class FinancialApiApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
	}

}
