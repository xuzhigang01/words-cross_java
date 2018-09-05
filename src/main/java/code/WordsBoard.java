package code;

import java.util.List;

public class WordsBoard {

	private Integer width;
	private Integer height;
	private List<LocatedWord> words;

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public List<LocatedWord> getWords() {
		return words;
	}

	public void setWords(List<LocatedWord> words) {
		this.words = words;
	}

	/**
	 * word with location info
	 */
	public static class LocatedWord {
		
		private String w;
		private Integer x;
		private Integer y;
		private Integer d;

		/**
		 * get the word, upper cased, e.g. FOOBAR
		 */
		public String getW() {
			return w;
		}

		/**
		 * set the word, upper cased, e.g. FOOBAR
		 */
		public void setW(String w) {
			this.w = w;
		}

		/**
		 * get horizontal value <br/>
		 * (x,y) represents a point, the top-left is (0,0).
		 */
		public Integer getX() {
			return x;
		}

		/**
		 * set horizontal value <br/>
		 * (x,y) represents a point, the top-left is (0,0).
		 */
		public void setX(Integer x) {
			this.x = x;
		}

		/**
		 * get vertical value <br/>
		 * (x,y) represents a point, the top-left is (0,0).
		 */
		public Integer getY() {
			return y;
		}

		/**
		 * set vertical value <br/>
		 * (x,y) represents a point, the top-left is (0,0).
		 */
		public void setY(Integer y) {
			this.y = y;
		}

		/**
		 * get direction
		 * 
		 * @return 0-horizontal,1-vertical
		 */
		public Integer getD() {
			return d;
		}

		/**
		 * set direction
		 * 
		 * @param d 0-horizontal,1-vertical
		 */
		public void setD(Integer d) {
			this.d = d;
		}
	}
}
