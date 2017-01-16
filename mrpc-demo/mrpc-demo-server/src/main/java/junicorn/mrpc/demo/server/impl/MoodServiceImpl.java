package junicorn.mrpc.demo.server.impl;


import junicorn.mrpc.demo.api.MoodService;
import junicorn.mrpc.demo.model.GENDER;
import junicorn.mrpc.demo.model.Mood;
import junicorn.mrpc.spring.annotation.MRpcService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MRpcService
public class MoodServiceImpl implements MoodService {

	@Override
	public Map<String, Mood> test(Integer value) {
		Map<String, Mood> map = new HashMap<String, Mood>();
		map.put("mood", new Mood());
		return map;
	}

	@Override
	public void test() { }

	@Override
	public String test(Integer value, String value1) {
		return value1 + ":" + value;
	}

	@Override
	public Integer test(String value1) {
		return 100;
	}

	@Override
	public Mood test(Mood value1) {
		return new Mood();
	}

	@Override
	public List<Mood> test(List<Mood> value1) {
		List<Mood> moods = new ArrayList<Mood>();
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		moods.add(new Mood());
		return moods;
	}

	@Override
	public Mood[] test(Mood[] value1) {
		return new Mood[]{new Mood(), new Mood(), new Mood()};
	}

	@Override
	public int[] test(int[] value1) {
		return new int[]{1, 2, 3, 4, 5};
	}

	@Override
	public int test(int value1,int value2) {
		return value1 + value2;
	}

	@Override
	public GENDER test(GENDER value1) {
		return GENDER.M;
	}
	
}
