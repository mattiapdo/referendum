package prove;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SortMapByValue
{
    public static boolean ASC = true;
    public static boolean DESC = false;

    public static void main(String[] args)
    {
    	/*
        // Creating dummy unsorted map
        Map<String, Integer> unsortMap = new HashMap<String, Integer>();
        unsortMap.put("B", 55);
        unsortMap.put("A", 80);
        unsortMap.put("D", 20);
        unsortMap.put("C", 70);

        System.out.println("Before sorting......");
        printMap(unsortMap);

        System.out.println("After sorting ascending order......");
        Map<String, Integer> sortedMapAsc = sortByComparator(unsortMap, ASC);
        printMap(sortedMapAsc);


        System.out.println("After sorting descindeng order......");
        Map<String, Integer> sortedMapDesc = sortByComparator(unsortMap, DESC);
        printMap(sortedMapDesc);
    	*/
    	Aula a1 = new Aula();
		a1.addAlunno("Mattia", 14);
		a1.addAlunno("Sergio", 12);
		a1.addAlunno("Claudia", 11);
		a1.addAlunno("Emma", 15);
		a1.addAlunno("Giovanni", 16);
		a1.addAlunno("Marco", 12);
		a1.addAlunno("Sara", 12);
		a1.addAlunno("Erica", 20);
		a1.addAlunno("Luigi", 23);
		
		Map<String, Alunno> unsortMap = a1.getAlunni();
		printMap(unsortMap);
		System.out.println("\n Sorted");
		Map<String, Alunno> sortedMapAsc = sortByComparator(unsortMap, DESC);
		printMap(sortedMapAsc);
    }

    private static Map<String, Alunno> sortByComparator(Map<String, Alunno> unsortMap, final boolean order)
    {

        List<Entry<String, Alunno>> list = new LinkedList<Entry<String, Alunno>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Alunno>>()
        {
            public int compare(Entry<String, Alunno> o1,
                    Entry<String, Alunno> o2)
            {
                if (order)
                {
                    return Integer.compare(o1.getValue().getAge(), o2.getValue().getAge());//o1.getValue().getAge().compareTo(o2.getValue().getAge());
                }
                else
                {
                    return Integer.compare(o2.getValue().getAge(), o1.getValue().getAge());//o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Alunno> sortedMap = new LinkedHashMap<String, Alunno>();
        for (Entry<String, Alunno> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
    


    public static void printMap(Map<String, Alunno> map)
    {
        for (Entry<String, Alunno> entry : map.entrySet())
        {
            System.out.println("Key : " + entry.getKey() + " Nome alunno : "+ entry.getValue().getName() + " Eta' : "+ entry.getValue().getAge());
        }
    }
}