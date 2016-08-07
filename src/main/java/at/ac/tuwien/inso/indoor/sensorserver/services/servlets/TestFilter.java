package at.ac.tuwien.inso.indoor.sensorserver.services.servlets;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by PatrickF on 14.04.2015.
 */
public class TestFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}
}
