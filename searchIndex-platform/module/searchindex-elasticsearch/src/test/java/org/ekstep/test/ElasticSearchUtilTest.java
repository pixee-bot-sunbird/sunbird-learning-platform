/**
 * 
 */
package org.ekstep.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.Test;

/**
 * @author pradyumna
 *
 */
public class ElasticSearchUtilTest extends BaseSearchTest {

	private static ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private static ObjectMapper mapper = new ObjectMapper();


	@Test
	public void testAddDocumentWithId() throws Exception {
		Map<String, Object> content = getContentTestRecord();
		String id = (String) content.get("identifier");
		addToIndex(id, content);
		String doc = elasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, id);
		assertTrue(StringUtils.contains(doc, id));
	}

	@Test
	public void testAddDocumentWithOutId() throws Exception {
		Map<String, Object> content = getContentTestRecord();
		content.remove("identifier");
		elasticSearchUtil.addDocument(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, mapper.writeValueAsString(content));
	}

	@Test
	public void testBulkIndexWithId() throws Exception {
		List<String> ids = new ArrayList<String>();
		Map<String, Object> jsonObjects = new HashMap<String, Object>();
		for (int i = 1; i <= 30; i++) {
			Map<String, Object> content = getContentTestRecord(null, i);
			String id = (String) content.get("identifier");
			ids.add(id);
			jsonObjects.put(id, content);
		}
		elasticSearchUtil.bulkIndexWithIndexId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, jsonObjects);
		List<String> resultDocs = elasticSearchUtil.getMultiDocumentAsStringByIdList(
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE,
				ids);

		assertNotNull(resultDocs);
		assertEquals(30, resultDocs.size());
	}

	@Test
	public void testBulkIndexWithAutoGenId() throws Exception {
		String id = null;
		List<Map<String, Object>> jsonObjects = new ArrayList<>();
		for (int i = 1; i <= 30; i++) {
			Map<String, Object> content = getContentTestRecord(null, i);
			id = (String) content.get("name");
			jsonObjects.add(content);
		}
		elasticSearchUtil.bulkIndexWithAutoGenerateIndexId(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, jsonObjects);
	}

	@Test
	public void testUpdateDocument() throws Exception {
		Map<String, Object> content = getContentTestRecord();
		String id = (String) content.get("identifier");
		addToIndex(id, content);
		content.put("name", "Content_" + System.currentTimeMillis() + "_name");
		elasticSearchUtil.updateDocument(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, mapper.writeValueAsString(content), id);
		String doc = elasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, id);
		assertTrue(StringUtils.contains(doc, id));
	}

	@Test
	public void testDelete() throws Exception {
		Map<String, Object> content = getContentTestRecord();
		String id = (String) content.get("identifier");
		addToIndex(id, content);
		elasticSearchUtil.deleteDocument(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, id);
		String doc = elasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE, id);
		assertFalse(StringUtils.contains(doc, id));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testSearchReturningId() throws Exception {
		Map<String, Object> content = getContentTestRecord();
		String id = (String) content.get("identifier");
		addToIndex(id, content);
		Map<String, Object> criteria = new HashMap<String, Object>();
		criteria.put("identifier", new ArrayList<>(Arrays.asList(id)));

		List<Map> result = elasticSearchUtil.textSearchReturningId(criteria,
				CompositeSearchConstants.COMPOSITE_SEARCH_INDEX, CompositeSearchConstants.COMPOSITE_SEARCH_INDEX_TYPE);

		assertNotNull(result);
		System.out.println("Search with ID: Size : " + result.size());
		// assertTrue(result.size() > 0);

	}

	private static Map<String, Object> getContentTestRecord() {
		String objectType = "Content";
		Date d = new Date();
		Map<String, Object> map = new HashMap<String, Object>();
		long suffix = (long) (10000000 + Math.random());
		map.put("identifier", "do_" + suffix);
		map.put("objectType", objectType);
		map.put("name", "Content_" + System.currentTimeMillis() + "_name");
		map.put("code", "code_" + System.currentTimeMillis());
		map.put("contentType", "Content");
		map.put("createdOn", new Date().toString());
		map.put("lastUpdatedOn", new Date().toString());
		map.put("lastPublishedOn", d.toString());
		map.put("status", "Draft");
		return map;
	}

	private static Map<String, Object> getContentTestRecord(String id, int index) {
		String objectType = "Content";
		Date d = new Date();
		Map<String, Object> map = getTestRecord(id, index, "do", objectType);
		map.put("name", "Content_" + System.currentTimeMillis() + "_name");
		map.put("code", "code_" + System.currentTimeMillis());
		map.put("contentType", getContentType());
		map.put("createdOn", new Date().toString());
		map.put("lastUpdatedOn", new Date().toString());
		if (index % 5 == 0) {
			map.put("lastPublishedOn", d.toString());
			map.put("status", "Live");
			map.put("size", 1000432);
		} else {
			map.put("status", "Draft");
			if (index % 3 == 0)
				map.put("size", 564738);
		}
		Set<String> tagList = getTags();
		if (null != tagList && !tagList.isEmpty() && index % 7 != 0)
			map.put("tags", tagList);
		map.put("downloads", index);
		return map;
	}

	private static Map<String, Object> getTestRecord(String id, int index, String prefix, String objectType) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(id))
			map.put("identifier", id);
		else {
			long suffix = 10000000 + index;
			map.put("identifier", prefix + "_" + suffix);
		}
		map.put("objectType", objectType);
		return map;
	}

	private static String[] contentTypes = new String[] { "Story", "Worksheet", "Game", "Collection", "Asset" };

	private static String getContentType() {
		return contentTypes[RandomUtils.nextInt(5)];
	}

	private static String[] tags = new String[] { "hindi story", "NCERT", "Pratham", "एकस्टेप", "हिन्दी",
			"हाथी और भालू", "worksheet", "test" };

	private static Set<String> getTags() {
		Set<String> list = new HashSet<String>();
		int count = RandomUtils.nextInt(9);
		for (int i = 0; i < count; i++) {
			list.add(tags[RandomUtils.nextInt(8)]);
		}
		return list;
	}

}
