package com.marketplace.helper;

import java.util.ArrayList;
import java.util.List;

import org.forgerock.json.JsonValue;
import org.json.JSONObject;

public class Test123 {

	public static void main(String[] args) {
		JsonValue jv = new JsonValue(null);

		List<String> al = new ArrayList<String>();

		al.add("John");

		jv.put("name", al);

		System.out.println(jv);

		JSONObject jsonobj = new JSONObject(jv.toString());

		System.out.println(jsonobj);

	}

}
