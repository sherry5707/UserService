package com.ragentek.ypush.service.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

public class DBUtil {

	private static DBHelper shelper = null;
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void saveOrUpdate(Context context, Object data, Class classzz) {
		Dao<Object, Integer> dao = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(classzz);
			// persist the account object to the database
			dao.createOrUpdate(data);
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Save data into table failed for class ".concat(classzz.getSimpleName()), e);
		}

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void delete(Context context, Object data, Class classzz) {
		Dao<Object, Integer> dao = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(classzz);
			// persist the account object to the database
			dao.delete(data);
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Save data into table failed for class ".concat(classzz.getSimpleName()), e);
		}

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void createIfNotExists(Context context, Object response, Class obj) {
		Dao<Object, Integer> dao = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(obj);
			// persist the account object to the database
			dao.createIfNotExists(response);
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Save data into table failed for class ".concat(obj.getSimpleName()), e);
		}

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static List<?> queryForAll(Context context, Class obj) {
		List<?> results = null;
		Dao<Object, Integer> dao = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(obj);
			results = dao.queryForAll();
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Query data from table failed for class ".concat(obj.getSimpleName()), e);
		}

		return results;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Object queryForEq(Context context, Class obj, String key, String value) {
		Object result = null;
		Dao<Object, Integer> dao = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(obj);
			List<?> list = dao.queryForEq(key, value);
			dao.getConnectionSource().close();
			if (!list.isEmpty()) {
				result = list.get(0);
			}
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Query data from table failed for class ".concat(obj.getSimpleName()), e);
		}

		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static List<?> queryForFieldValues(Context context, Class obj, Map<String, Object> query) {
		Dao<Object, Integer> dao = null;
		List<?> results = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(obj);
			results = dao.queryForFieldValues(query);
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Query data from table failed for class ".concat(obj.getSimpleName()), e);
		}

		return results;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static int count(Context context, Class obj, String query) {
		int count = 0;
		Dao<Object, Integer> dao = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(obj);

			GenericRawResults<String[]> rawResults = dao.queryRaw(query);

			// there should be 1 result

			List<String[]> results = rawResults.getResults();

			// the results array should have 1 value

			String[] resultArray = results.get(0);
			dao.getConnectionSource().close();
			count = Integer.parseInt(resultArray[0]);
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Count data from table failed for class ".concat(obj.getSimpleName()), e);
		}

		return count;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static int countAll(Context context, Class obj) {
		Dao<Object, Integer> dao = null;
		int count = 0;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(obj);
			count = (int) dao.countOf();
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Count data from table failed for class ".concat(obj.getSimpleName()), e);
		}

		return count;

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void delete(Context context, Class obj, String query) {
		Dao<Object, Integer> dao = null;
		try {
			DBHelper helper = initDbHelper(context);
			dao = helper.getDao(obj);

			dao.queryRaw(query);
			dao.getConnectionSource().close();
		} catch (Exception e) {
			if (dao != null) {
				try {
					dao.getConnectionSource().close();
				} catch (SQLException e1) {
					// do nothing
				}
			}
			Log.e(DBUtil.class.getSimpleName(), "Count data from table failed for class ".concat(obj.getSimpleName()), e);
		}

	}

	public static DBHelper initDbHelper(Context context) {

		if (shelper == null) {
			shelper = new DBHelper(context);
		}
		return shelper;
	}

}