package junicorn.mrpc.demo.api;

import junicorn.mrpc.demo.model.GENDER;
import junicorn.mrpc.demo.model.Mood;

import java.util.List;
import java.util.Map;

public interface MoodService {

	void test();
	
	Map<String, Mood> test(Integer value);
	
	Integer test(String value1);
	
	String test(Integer value, String value1);
	
	Mood test(Mood value1);
	
	List<Mood> test(List<Mood> value1);
	
	Mood[] test(Mood[] value1);
	
	int[] test(int[] value1);
	
	int test(int value1, int value2);
	
	GENDER test(GENDER value1);

}