package es.cristichi.cardphotocopier.obj;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CardComparator implements Comparator<CardInfo> {

	private List<String> order;

	public CardComparator(String... order) {
		this.order = Arrays.asList(order);
		System.out.println("Order chosen for the cards: "+this.order.toString());
	}

	@Override
	public int compare(CardInfo o1, CardInfo o2) {
		if (order.contains(o1.type) && order.contains(o2.type)) {
			return order.indexOf(o1.type) - order.indexOf(o2.type);
		}
		if (order.contains(o1.type)) {
			return -1;
		}
		if (order.contains(o2.type)) {
			return 1;
		}

//		return o1.name.compareTo(o2.name);
		return Integer.compare(o1.row, o2.row);
	}

}
