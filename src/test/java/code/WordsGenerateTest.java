package code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.junit.Test;

import code.WordsGenerateServlet;

public class WordsGenerateTest {

	@Test
	public void TestGenWords() throws ServletException {
		WordsGenerateServlet servlet = new WordsGenerateServlet();
		servlet.init();
		Set<String> words = WordsGenerateServlet.genWords("restore");
		System.out.println(words);
	}
	
	//@Test
	public void createDict() {
		List<String> result = new LinkedList<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader("/home/xzg/Downloads/d.txt"))) {
			String line;
			int len;
			char c;
			boolean valid;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				len = line.length();
				if (len < 4 || len > 8) {
					continue;
				}
				line = line.toLowerCase();
				valid = true;
				for (int i = 0; i < len; i++) {
					c = line.charAt(i);
					if (c < 'a' || c > 'z') {
						valid = false;
						break;
					}
				}
				if (!valid) {
					continue;
				}
				result.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter("/home/xzg/Downloads/dict.txt"))) {
			for (String s : result) {
				bw.write(s);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("write OK!");
	}
}
