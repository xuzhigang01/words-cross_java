package code;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class WordsCrossBuildServlet extends HttpServlet {
    private static final long serialVersionUID = -5607690679212746893L;
    private static final Logger LOG = LoggerFactory.getLogger(WordsCrossBuildServlet.class);

    private static final String DEFAULT_CHAR_ENCODE = "utf-8";

    /**
     * POST /buildcross [words=abcd,dbca,adb] 构建单词交叉
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String words = req.getParameter("words");
        LOG.info("POST {} [words={}]", req.getRequestURI(), words);

        try {
            WordsBoard board = WordsCrossBuilder.build(words);

            resp.setStatus(200);
            resp.setContentType("application/json");
            resp.setCharacterEncoding(DEFAULT_CHAR_ENCODE);
            resp.getWriter().write(JSON.toJSONString(board));
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                LOG.error("WordsCrossBuilder.build failed, {}", e.getMessage());
            } else {
                LOG.error("WordsCrossBuilder.build failed", e);
            }
            resp.setStatus(500);
            resp.setContentType("text/html");
            resp.setCharacterEncoding(DEFAULT_CHAR_ENCODE);
            resp.getWriter().write(e.getMessage());
        }
    }
}
