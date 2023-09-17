package es.cristichi.cardphotocopier.obj;

public class Range {
	public int low, high;

	public Range(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	public boolean inRange(int number) {
		return number >= low && number <= high;
	}
}
