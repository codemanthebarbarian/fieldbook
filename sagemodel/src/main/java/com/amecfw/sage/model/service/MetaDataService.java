package com.amecfw.sage.model.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import com.amecfw.sage.model.EqualityComparator;
import com.amecfw.sage.model.MetaDataDescriptor;
import com.amecfw.sage.model.MetaDataSupport;
import com.amecfw.sage.model.MetaElement;
import com.amecfw.sage.persistence.DaoSession;
import com.amecfw.sage.ui.MetaDataListDialogFragment;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.WhereCondition;

public class MetaDataService {
	DaoSession session;
	
	public MetaDataService(DaoSession session){
		this.session = session;
	}
	
	/**
	 * finds a list of MetaDataSupport objects based on the name and value of the meta data element.
	 * if metaName or metaValue are null, query uses 'is null' statements for the corresponding value.
	 * @param clazz
	 * @param metaTableName
	 * @param metaName
	 * @param metaValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <TObj extends MetaDataSupport<?>> List<TObj> find(Class<?> clazz, String metaTableName, String metaName, String metaValue){
		AbstractDao<?, ?> dao = session.getDao(clazz);
		String query = "inner join " + metaTableName + " M  on T._id = M.PARENT_ID";
		List<TObj> results = null;
		if(metaName == null && metaValue == null)
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.NAME is null  and M.VALUE is null"));
		else if (metaName == null)
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.NAME is null  and M.VALUE = ?"), metaValue);
		else if(metaValue == null)
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.NAME = ?  and M.VALUE is null"), metaName);
		else
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.NAME = ?  and M.VALUE = ?"), metaName , metaValue);
		if(results == null) results = new ArrayList<TObj>();
		return results;
	}
	
	/**
	 * finds a list of MetaDataSupport objects based on the name of the meta data element.
	 * if metaName is null, query uses 'is null' statements for the corresponding name.
	 * @param clazz
	 * @param metaTableName
	 * @param metaName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <TObj extends MetaDataSupport<?>> List<TObj> findByMetaName(Class<?> clazz, String metaTableName, String metaName){
		AbstractDao<?, ?> dao = session.getDao(clazz);
		String query = "inner join " + metaTableName + " M  on T._id = M.PARENT_ID";
		List<TObj> results = null;
		if (metaName == null)
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.NAME is null"));
		else
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.NAME = ?"), metaName);
		if(results == null) results = new ArrayList<TObj>();
		return results;
	}
	
	/**
	 * finds a list of MetaDataSupport objects based on the value of the meta data element.
	 * if metaValue is null, query uses 'is null' statements for the corresponding value.
	 * @param clazz
	 * @param metaTableName
	 * @param metaValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <TObj extends MetaDataSupport<?>> List<TObj> findByMetaValue(Class<?> clazz, String metaTableName, String metaValue){
		AbstractDao<?, ?> dao = session.getDao(clazz);
		String query = "inner join " + metaTableName + " M  on T._id = M.PARENT_ID";
		List<TObj> results = null;
		if (metaValue == null)
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.VALUE is null"));
		else
			results = (List<TObj>) dao.queryRaw(query.concat(" where M.VALUE = ?"), metaValue);
		if(results == null) results = new ArrayList<TObj>();
		return results;
	}
	
	/**
	 * Updates the metadata in the database for the provided source using the supplied list of meta elements already found in the 
	 * database for the provided source. Any existing elements found in the parent and in the persisted collection are updated. Any found
	 * in the database but not in the source are deleted, and any found in the source and not in the database are added. 
	 * 
	 * @param source the parent object
	 * @param dao the meta elements dao
	 */
	public static <TmetaElement extends MetaElement> void update(MetaDataSupport<TmetaElement> source, AbstractDao<TmetaElement, Long> dao){
		MetaDataService.MetaNameComparor comparator = new  MetaDataService.MetaNameComparor();
		if(source.hasMetaData()){
		List<TmetaElement> persisted = dao.queryBuilder().where(new WhereCondition.StringCondition(String.format(" PARENT_ID = %d", source.getId()))).list();
		for (TmetaElement metaElement : persisted) {
			metaElement.setComparator(comparator);
			int index = source.getMetaData().indexOf(metaElement);
			if(index > -1){ //found update the element
				TmetaElement trans = source.getMetaData().get(index);
				metaElement.setValue(trans.getValue());
				dao.update(metaElement);
				source.getMetaData().set(index, metaElement);
			}else{// not found so delete it
				dao.delete(metaElement);
			}
		}
		for (TmetaElement elementMeta : source.getMetaData()) {
			if(elementMeta.getId() == null || elementMeta.getId() < 1 ) {
				if(elementMeta.getRowGuid() == null) elementMeta.setRowGuid();
				elementMeta.setParentID(source.getId());
				dao.insert(elementMeta);
			}
		}
		}else{
			delete(source, dao);
		}
	}
	
	/**
	 * inserts the metadata into the database, provided that the source has meta data. The source must already be saved in the database (at least have an ID).
	 * @param source
	 * @param dao
	 */
	public static <TmetaElement extends MetaElement> void save(MetaDataSupport<TmetaElement> source, AbstractDao<TmetaElement, Long> dao){
		if(!source.hasMetaData()) return;
		for (TmetaElement meta : source.getMetaData()) {
			meta.setParentID(source.getId());
			meta.setRowGuid();
			dao.insert(meta);
		}
	}
	
	public static <TmetaElement extends MetaElement> void delete(MetaDataSupport<TmetaElement> source, AbstractDao<TmetaElement, Long> dao){
		dao.queryBuilder().where(new WhereCondition.StringCondition(" PARENT_ID = ?", source.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
	}
	 
	/**
	 * Generates a list of meta elements based on the descriptors in the source class. The descriptor must have the fully qualified class name to 
	 * instantiate the correct meta element. If any exception are thrown of any descriptor null is returned. If the source has no MetaElementDescriptors, 
	 * an empty list is returned. If no default value is provided for a descriptor and the annotated field is null, the value for the meta element 
	 * will be an empty string if ignoreEmptyStrings is false otherwise the element will not be added to the list.
	 * @param ignoreEmptyStrings true if do not add elements with empty strings
	 * @param source
	 * @return A list of converted elements, and empty list if there are not annotations, or null if there was an error processing annotations
	 */
	@SuppressWarnings("unchecked")
	public static <Tsource, TmetaElement extends MetaElement> List<TmetaElement> fromAnnotations(Tsource source, boolean ignoreEmptyStrings){
		Field[] fields = source.getClass().getDeclaredFields();
		List<TmetaElement> metaElements = new ArrayList<TmetaElement>();
		for(Field field : fields){
			MetaDataDescriptor descriptor = field.getAnnotation(MetaDataDescriptor.class);
			if(descriptor != null){
				field.setAccessible(true);
				try{
					String value = (String) field.get(source);
					if(value == null) value = descriptor.defaultValue();
					TmetaElement metaElement = (TmetaElement) descriptor.clazz().newInstance();
					metaElement.setName(descriptor.metaDataName());
					metaElement.setValue(value);
					if(value != "" || !ignoreEmptyStrings) metaElements.add(metaElement);
				}catch (IllegalAccessException | IllegalArgumentException 
						| InstantiationException | ClassCastException e) {
					Log.e("MetaDataService", String.format("%s.fromAnnotations : %s", e.getClass().getSimpleName(), e.getMessage()));
					return null;
				}
			}
		}
		return metaElements;
	}
	
	/**
	 * Updates the annotated fields in the annotated source from the values in the provided meta elements. Returns the number of fields updated based
	 * on what was available in the provided meta elements. If there is an error processing an element -1 is returned. If there are not fields annotated or
	 * not matching elements are found, 0 will be returned.
	 * @param annotatedSource
	 * @param metaElements
	 * @return the number of matching elements or -1 if there is an error processing elements or fields.
	 */
	@SuppressWarnings("unchecked")
	public static <Tsource, TmetaElement extends MetaElement> int updateAnnotations(Tsource annotatedSource, List<TmetaElement> metaElements){
		int result = 0;
		if(metaElements == null || metaElements.size() == 0) return result;
		Field[] fields = annotatedSource.getClass().getDeclaredFields();
		MetaNameComparor comparator = new MetaNameComparor();
		for (Field field : fields){
			MetaDataDescriptor descriptor = field.getAnnotation(MetaDataDescriptor.class);
			if(descriptor != null){
				field.setAccessible(true);
				try {
					TmetaElement metaElement = (TmetaElement) descriptor.clazz().newInstance();
					metaElement.setName(descriptor.metaDataName());
					metaElement.setComparator(comparator);
					int index = metaElements.indexOf(metaElement);
					if(index > -1){
						String value = metaElements.get(index).getValue();
						field.set(annotatedSource, value);
						result++;
					}
				} catch (InstantiationException | IllegalAccessException | ClassCastException e) {
					Log.e("MetaDataService", String.format("%s.updateAnnotations : %s", e.getClass().getSimpleName(), e.getMessage()));
					return -1;
				}
			}
		}			
		return result;
	}
	
	public static class MetaSupportExtensionMethods{
		
		/**
		 * Merge the meta elements from two objects. If duplicates exist the destination's will be overwritten by the source.
		 * @param destination The object to copy/merge the elements to
		 * @param source the source to copy elements from
		 */
		public static <TmetaElement extends MetaElement> void merge(MetaDataSupport<TmetaElement> destination, MetaDataSupport<TmetaElement> source){
			if(!(destination.hasMetaData() && source.hasMetaData())) return;
			if(! destination.hasMetaData()) {
				destination.setMetaData(source.getMetaData());
			} else if(!source.hasMetaData()) {
				return;
			} else{
				for(TmetaElement meta: source.getMetaData()){
					addUpdate(destination, meta);
				}
			}				
		}
		
		/**
		 * replace the meta elements in the destination with the ones provided in the source. If there is a match, the destination is updated to 
		 * reflect the changes provided in the source. Any not existing in the destination that are found in the source are added and any found
		 * in the destination that are not in the source are removed.
		 * @param destination
		 * @param source
		 */
		public static <TmetaElement extends MetaElement> void replace(MetaDataSupport<TmetaElement> destination, MetaDataSupport<TmetaElement> source){
			if(!destination.hasMetaData() && !source.hasMetaData()) return;
			if(! source.hasMetaData()) {
				destination.resetMetaData();
				return;
			}
			for(TmetaElement element : source.getMetaData()) addUpdate(destination, element);
			if(destination.getMetaData().size() > source.getMetaData().size()){//remove the ones for deletion
				MetaNameComparor comparator = new MetaNameComparor();
				for(int i = destination.getMetaData().size() ; i < 0 ; i--){
					TmetaElement element = destination.getMetaData().get(i);
					element.setComparator(comparator);
					if(!source.getMetaData().contains(element)) destination.getMetaData().remove(i);
				}
			}
		}
		
		/**
		 * add or updates the meta element with the same name, if one exists the existing one is returned otherwise null is returned.
		 * @param source
		 * @param metaElement
		 * @return
		 */
		public static <TmetaElement extends MetaElement> TmetaElement addUpdate(MetaDataSupport<TmetaElement> source, TmetaElement metaElement){
			if(source.hasMetaData()){
				metaElement.setComparator(new MetaNameComparor());
				int index = source.getMetaData().indexOf(metaElement);
				if(index < 0) {
					source.getMetaData().add(metaElement);
					return metaElement;
				}
				else{
					TmetaElement existing = source.getMetaData().get(index);
					existing.setValue(metaElement.getValue());
					return existing;
				}
				
			}else{
				source.setMetaData(new ArrayList<TmetaElement>());
				source.getMetaData().add(metaElement);
				return metaElement;
			}
		}
		
		public static <TmetaElement extends MetaElement> TmetaElement find(MetaDataSupport<TmetaElement> source, String name){
			if(!source.hasMetaData()) return null;
			for(TmetaElement meta : source.getMetaData()){
				if(meta.getName().equalsIgnoreCase(name)) return meta;
			}
			return null;
		}
		
		/**
		 * attempts to add or update a new metaelement, if unable will return null
		 * @param source
		 * @param clazz
		 * @param name
		 * @param value
		 * @return
		 */
		public static <TmetaElement extends MetaElement> TmetaElement addUpdate(MetaDataSupport<TmetaElement> source, Class<TmetaElement> clazz
				, String name, String value){
			TmetaElement element;
			try {
				element = clazz.newInstance();
				element.setComparator(new MetaNameComparor());
				element.setName(name);
				element.setValue(value);
				return addUpdate(source, element);
			} catch (InstantiationException | IllegalAccessException e) {
				return null;
			}			
		}
		
	}
	
	public static <T extends MetaElement> List<MetaDataListDialogFragment.ViewModel> convertToViewModel(List<T> metaElements){
		if(metaElements == null) return null;
		List<MetaDataListDialogFragment.ViewModel> results = new ArrayList<MetaDataListDialogFragment.ViewModel>(metaElements.size());
		for(T element : metaElements){
			MetaDataListDialogFragment.ViewModel viewModel = new MetaDataListDialogFragment.ViewModel();
			viewModel.name = element.getName();
			viewModel.value = element.getValue();
			viewModel.rowGuid = element.getRowGuid();
			results.add(viewModel);
		}
		return results;
	}
	
	public static class MetaViewModelNameComparor implements EqualityComparator{
		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof MetaDataListDialogFragment.ViewModel)) return false;
			if(!(objB instanceof MetaDataListDialogFragment.ViewModel)) return false;
			MetaDataListDialogFragment.ViewModel a = (MetaDataListDialogFragment.ViewModel) objA;
			MetaDataListDialogFragment.ViewModel b = (MetaDataListDialogFragment.ViewModel) objB;
			if(a.name == null || b.name == null) return false;
			return a.name.equalsIgnoreCase(b.name);
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof MetaDataListDialogFragment.ViewModel)) return obj.hashCode();
			String name = ((MetaDataListDialogFragment.ViewModel)obj).name;
			return name == null ? 0 : name.toLowerCase(Locale.getDefault()).hashCode();
		}
	}
	
	public static class MetaNameComparor implements EqualityComparator{

		@Override
		public boolean equalsTo(Object objA, Object objB) {
			if(objA == null || objB == null) return false;
			if(!(objA instanceof MetaElement)) return false;
			if(!(objB instanceof MetaElement)) return false;
			MetaElement a = (MetaElement) objA;
			MetaElement b = (MetaElement) objB;			
			return a.getName().equalsIgnoreCase(b.getName());
		}

		@Override
		public int getHashCode(Object obj) {
			if(obj == null) return 0;
			if(!(obj instanceof MetaElement)) return obj.hashCode();
			String name = ((MetaElement)obj).getName();
			return name == null ? 0 : name.toLowerCase(Locale.getDefault()).hashCode();
		}
		
	}
	
}
