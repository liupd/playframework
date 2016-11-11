package org.apache.playframework.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.playframework.service.FieldFillService;
import org.apache.playframework.util.ReflectUtils;
import org.apache.playframework.util.SpringUtils;

import com.baomidou.framework.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.annotations.IdType;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.toolkit.TableInfo;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;

public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<BaseMapper<T>, T>{

	private  FieldFillService fieldFillService;
	
	public BaseServiceImpl() {
		if (SpringUtils.getBeanFactory() != null && SpringUtils.containsBean("fieldFillService")) {
			fieldFillService = (FieldFillService)SpringUtils.getBean("fieldFillService");
        } else {
        	fieldFillService = new FieldFillServiceDefaultImpl();
        }
	}
	
	private void setInsertData(T entity) {
		Map<String, Object> insertData = fieldFillService.getInsertData();
		for (Map.Entry<String, Object> entry : insertData.entrySet()) {
			ReflectUtils.setProperty(entity, entry.getKey(), entry.getValue());
		}
	}

	private void setInsertData(List<T> list) {
		Map<String, Object> insertData = fieldFillService.getInsertData();
		for (T t : list) {
			for (Map.Entry<String, Object> entry : insertData.entrySet()) {
				ReflectUtils.setProperty(t, entry.getKey(), entry.getValue());
			}
		}
	}
	
	private void setUpdateData(T entity) {
		Map<String, Object> updateData = fieldFillService.getUpdateData();
		for (Map.Entry<String, Object> entry : updateData.entrySet()) {
			ReflectUtils.setProperty(entity, entry.getKey(), entry.getValue());
		}
	}
	
	private void setUpdateData(List<T> list) {
		Map<String, Object> updateData = fieldFillService.getUpdateData();
		for (T t : list) {
			for (Map.Entry<String, Object> entry : updateData.entrySet()) {
				ReflectUtils.setProperty(t, entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public boolean insertOrUpdate(T entity) {
		if (null != entity) {
			Class<?> cls = entity.getClass();
			TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
			if (null != tableInfo) {
				Object idVal = ReflectionKit.getMethodValue(cls, entity, tableInfo.getKeyProperty());
				if (null == idVal || "".equals(idVal)) {
					return insert(entity);
				} else {
					/* 特殊处理 INPUT 主键策略逻辑 */
					if (IdType.INPUT == tableInfo.getIdType()) {
						T entityValue = selectById((Serializable) idVal);
						if (null != entityValue) {
							setUpdateData(entity);
							return updateById(entity);
						} else {
							setInsertData(entity);
							return insert(entity);
						}
					}
					setUpdateData(entity);
					return updateById(entity);
				}
			} else {
				throw new MybatisPlusException("Error:  Cannot execute. Could not find @TableId.");
			}
		}
		return false;
	}

	@Override
	public boolean insert(T entity) {
		setInsertData(entity);
		return super.insert(entity);
	}

	@Override
	public boolean insertBatch(List<T> entityList) {
		setInsertData(entityList);
		return super.insertBatch(entityList);
	}

	@Override
	public boolean insertBatch(List<T> entityList, int batchSize) {
		setInsertData(entityList);
		return super.insertBatch(entityList, batchSize);
	}


	@Override
	public boolean updateById(T entity) {
		setUpdateData(entity);
		return super.updateById(entity);
	}

	@Override
	public boolean update(T entity, Wrapper<T> wrapper) {
		setUpdateData(entity);
		return super.update(entity, wrapper);
	}

	@Override
	public boolean updateBatchById(List<T> entityList) {
		setUpdateData(entityList);
		return super.updateBatchById(entityList);
	}

	
}