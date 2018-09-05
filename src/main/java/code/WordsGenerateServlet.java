package code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class WordsGenerateServlet extends HttpServlet {
    private static final long serialVersionUID = 4272041712511665032L;
    private static final Logger LOG = LoggerFactory.getLogger(WordsGenerateServlet.class);

    private static final String DEFAULT_CHAR_ENCODE = "utf-8";
    private static final Set<String> WORD_DICT = new HashSet<>();

    private static final Comparator<String> LENDESC_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o2.length() - o1.length();
        }
    };

    public void init() throws ServletException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/dict.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                WORD_DICT.add(line);
            }
        } catch (IOException e) {
            throw new ServletException("read dict.txt failed", e);
        }
    }

    /**
     * GET /genwords?letters=abcde 生成单词
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("GET {}?{}", req.getRequestURI(), req.getQueryString());

        String letters = req.getParameter("letters");

        Set<String> words = genWords(letters.toLowerCase());
        List<String> list = top20Long(words);

        resp.setStatus(200);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(DEFAULT_CHAR_ENCODE);
        resp.getWriter().write(JSON.toJSONString(list));
    }

    /** 生成单词：字母组合成单词，返回长度4-8、且在字典中的单词 */
    protected static Set<String> genWords(String letters) {
        Set<String> set = new HashSet<>();
        for (char c : letters.toCharArray()) {
            Set<String> tset = new HashSet<>(set);
            tset.add(String.valueOf(c));
            for (String s : set) {
                tset.add(c + s);
                for (int i = 1, len = s.length(); i < len; i++) {
                    tset.add(s.substring(0, i) + c + s.substring(i));
                }
                tset.add(s + c);
            }
            set = tset;
        }

        Iterator<String> iter = set.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            if (s.length() < 4 || s.length() > 8 || !WORD_DICT.contains(s)) {
                iter.remove();
            }
        }

        return set;
    }

    /** 如果单词数量>20，返回最长的前20个 */
    private static List<String> top20Long(Set<String> set) {
        List<String> list = new ArrayList<>(set);
        if (list.size() > 20) {
            Collections.sort(list, LENDESC_COMPARATOR);
            list = new ArrayList<>(list.subList(0, 20));
        }
        Collections.sort(list);
        return list;
    }
}
