package jade1;

import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.HashMap;
import jade.util.leap.LinkedList;
import jade.util.leap.List;
import jade1.CountingAgent;
import jade1.DistributorAgent;

public class main {
	private static JSONObject toJSON(List list) {
		JSONObject obj = new JSONObject();
		for (int i = 0; i < list.size(); i++) {
			JSONObject obj2 = new JSONObject();
			List l = (List) list.get(i);
			for (int j = 0; j < l.size(); j++) {
				obj2.put("" + j, l.get(j));
			}
			obj.put("" + i, obj2);
		}
		return obj;
	}
	
	
	static Random rand = new Random();
	private static int initialBowSize = 10;
	private static ArrayList food = new ArrayList();
	private static String[] foodTypes = {
			"kebab", "hamburger", "nudle", "apple", "soup", 
			"beer", "cake", "eggs", "pork", "chicken", 
			"pie", "fish", "rise", "salad", "sandwich", "stew"};
	private static String getRandomFood() {
		return foodTypes[rand.nextInt(foodTypes.length)];
	}

	public static void main(String[] args) {

		for (int i=0; i<initialBowSize; i++) {
			food.add(getRandomFood());
		}
		System.out.println(String.join(" ,", food.toList()));
		
		
		
		
		
		int ILOSC_POTRZEBNYCH_AGENTOW = 2;

		List agents = new LinkedList();
		List matrix = new ArrayList();
		List matrixResult = new ArrayList();
		List rowTODO = new ArrayList();

		List wyniki = new LinkedList();

		for (int i = 0; i < 4; i++) {
			List list2 = new ArrayList();
			for (int j = 0; j < 4; j++) {
				list2.add(j);
			}
			matrix.add(list2);
		}
		for (int i = 0; i < ILOSC_POTRZEBNYCH_AGENTOW; i++) {
			rowTODO.add(i);
		}

		// PETLA GLOWNA
		for (int p = 0; p < ILOSC_POTRZEBNYCH_AGENTOW; p++) {

			int rowToCount = (int) rowTODO.get(0);
			rowTODO.remove(0);

			JSONObject obj = new JSONObject();
			obj.put("m1", toJSON(matrix));
			obj.put("m2", toJSON(matrix));
			obj.put("threads", ILOSC_POTRZEBNYCH_AGENTOW);
			obj.put("id", rowToCount);
			String OBJECT = obj.toJSONString();

			agents.add(rowToCount);

			// cal
			JSONParser parser = new JSONParser();
			JSONObject myObject;
			try {
				myObject = (JSONObject) parser.parse((String) OBJECT);

				long id = (long) myObject.get("id");
				long threads = (long) myObject.get("threads");
				JSONObject m1 = (JSONObject) myObject.get("m1");
				JSONObject m2 = (JSONObject) myObject.get("m2");

				List matrix1 = new ArrayList();
				for (int i = 0; i < m1.size(); i++) {
					JSONObject matrixI = (JSONObject) m1.get(""+i);
					List list2 = new ArrayList();
					for (int j = 0; j < matrixI.size(); j++) {
						list2.add(matrixI.get(""+j));
					}
					matrix1.add(list2);
				}
				List matrix2 = new ArrayList();
				for (int i = 0; i < m2.size(); i++) {
					JSONObject matrixI = (JSONObject) m2.get(""+i);
					List list2 = new ArrayList();
					for (int j = 0; j < matrixI.size(); j++) {
						list2.add(matrixI.get(""+j));
					}
					matrix2.add(list2);
				}

				double chunk = ((double) matrix1.size()) / ((double) threads);
				double decimal = chunk % 1;
				chunk = Math.floor(chunk);
				int extraWork = (int) Math.round(decimal * ((double) threads));
				long extraWorkStart = id == 0 ? 0 : (id < extraWork ? id : extraWork);
				long start = (id * ((int) chunk)) + extraWorkStart;
				long end = start + ((int) chunk) + (id < extraWork ? 1 : 0);

				List wynik = new ArrayList();
				for (long count = start; count < end; count++) {
					List list2 = new ArrayList();
					for (long count2 = 0; count2 < ((List) matrix2.get(0)).size(); count2++) {
						long suma = 0;
						for (long count3 = 0; count3 < ((List) matrix1.get(0)).size(); count3++) {
							suma += ((long) ((List) matrix1.get((int) count)).get((int) count3))
									* ((long) ((List) matrix2.get((int)count3)).get((int) count2));
						}
						list2.add(suma);
					}
					wynik.add(list2);
				}

				// calculation end

				JSONObject obj2 = new JSONObject();
				obj2.put("result", toJSON(wynik));
				obj2.put("id", id);

				wyniki.add(obj2.toJSONString());

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		for (int i = 0; i < agents.size(); i++) {
			String content = (String) wyniki.get(i);
			matrixResult.add(content);
			if (matrixResult.size() == ILOSC_POTRZEBNYCH_AGENTOW) {
				HashMap hmap = new HashMap();

				try {
					for (int k = 0; k < matrixResult.size(); k++) {
						JSONParser parser = new JSONParser();
						JSONObject myObject = (JSONObject) parser.parse((String) matrixResult.get(k));
						long id = (long) myObject.get("id");
						hmap.put("" + id, (JSONObject) myObject.get("result"));
					}
					String WholeMatrix = "";
					for (int h=0; h<hmap.size(); h++) {
						JSONObject f = (JSONObject) hmap.get(""+h);
						for (int jj = 0; jj < f.size(); jj++) {
							JSONObject fjj = (JSONObject) f.get(""+jj);
							String row = "";
							for (int jja = 0; jja < fjj.size(); jja++) {
								long q = (long) fjj.get(""+jja);
								row += q + "\t";	
							}
							WholeMatrix += row + "\n";
						}
					}
					// log("\nWYNIK:\n"+WholeMatrix);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}

	}

}