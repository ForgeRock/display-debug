package com.marketplace.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.forgerock.json.JsonValue;
import org.json.JSONObject;

public class Test123 {

	public static void main(String[] args) {


		List<String> al = new ArrayList<String>();

		al.add("John");

		
		JsonValue jv = new JsonValue(al);

		System.out.println(jv);

		Map<String, Object> content = new LinkedHashMap<String, Object>();
		content.put("names", jv);
		
		JSONObject jsonobj = new JSONObject(content);

		System.out.println(jsonobj);

	}

}
