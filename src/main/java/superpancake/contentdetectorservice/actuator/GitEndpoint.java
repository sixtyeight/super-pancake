package superpancake.contentdetectorservice.actuator;

import java.io.IOException;
import java.util.Properties;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnResource(resources = "git.properties")
public class GitEndpoint extends AbstractEndpoint<GitRepositoryState> {

	private GitRepositoryState state = new GitRepositoryState();

	public GitEndpoint() {
		super("git");

		try {
			Properties git = new Properties();
			git.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("git.properties"));

			state.git = git;
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	@Override
	public GitRepositoryState invoke() {
		return state;
	}

}