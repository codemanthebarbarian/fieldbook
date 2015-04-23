package com.amecfw.sage.dao;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class SageDaoGenerator {
    //map for managing class relationships
    private static Map<String, Entity> entities = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(7, "com.amecfw.sage.model");
        schema.setDefaultJavaPackageDao("com.amecfw.sage.persistence");
        schema.setDefaultJavaPackageTest("com.amecfw.sage.test");
        addOwner(schema);
        addProject(schema);
        addSite(schema);
        addLocation(schema);
        addProjectSite(schema);
        addCoordinate(schema);
        addElement(schema);
        addElementGroup(schema);
        addStation(schema);
        addStationElement(schema);
        addObservationType(schema);
        addObservationGroup(schema);
        addObservation(schema);
        addParameter(schema);
        addMeasurement(schema);
        addPhotos(schema);
        String srcPath = "../model/src/main/java/";
        String testPath = "../model/src/androidTest/java/";
        DaoGenerator generator = new DaoGenerator();
        generator.generateAll(schema, srcPath, testPath);
    }

    private static void setEntityBase(Entity entity){
        entity.setHasKeepSections(true);
        entity.setSuperclass("EntityBase");
        entity.implementsInterface("UUIDSupport");
        //entity.implementsSerializable();
        entity.addIdProperty();
        entity.addStringProperty("rowGuid").unique().notNull();
    }

    private static void makeParcelable(Entity entity){
        entity.implementsInterface("Parcelable");
    }

    private static void setMetaElement(Schema schema, String elementName, Entity parent){
        parent.implementsInterface(String.format("MetaDataSupport<%s>", elementName));
        Entity meta = schema.addEntity(elementName);
        meta.implementsInterface("MetaElement");
        entities.put(elementName, meta);
        setEntityBase(meta);
        meta.addStringProperty("name");
        meta.addStringProperty("value");
        Property parentID = meta.addLongProperty("parentID").notNull().getProperty();
        meta.addToOne(parent, parentID);
        ToMany metaData = parent.addToMany(meta, parentID);
        metaData.setName("metaData");
    }

    private static void setOwner(Entity entity){
        entity.implementsInterface("Ownership");
        Property owner = entity.addLongProperty("ownerID").getProperty();
        entity.addToOne(entities.get("Owner"), owner);
    }

    public static void addOwner(Schema schema){
        Entity owner = schema.addEntity("Owner");
        entities.put("Owner", owner);
        setEntityBase(owner);
        makeParcelable(owner);
        owner.addStringProperty("name").notNull();
        owner.addStringProperty("type").notNull();
    }

    private static void addProject(Schema schema){
        Entity project = schema.addEntity("Project");
        entities.put("Project", project);
        setEntityBase(project);
        project.addStringProperty("projectNumber");
        project.addStringProperty("name");
        project.addStringProperty("node");
        Property rootProperty = project.addLongProperty("rootID").getProperty();
        project.addToOne(project, rootProperty).setName("root");
    }

    private static void addSite(Schema schema){
        Entity site = schema.addEntity("Site");
        entities.put("Site", site);
        setEntityBase(site);
        site.addStringProperty("name");
        site.addStringProperty("node");
        Property rootProperty = site.addLongProperty("rootID").getProperty();
        site.addToOne(site, rootProperty).setName("root");
    }

    private static void addLocation(Schema schema) {
        Entity location = schema.addEntity("Location");
        entities.put("Location", location);
        setEntityBase(location);
        location.addStringProperty("elevation");
        location.addStringProperty("latitude");
        location.addStringProperty("longitude");
        location.addStringProperty("name");
        location.addStringProperty("nema");
        Property siteProperty = location.addLongProperty("siteID").getProperty();
        location.addToOne(entities.get("Site"), siteProperty).setName("site");
        setMetaElement(schema, "LocationMeta", location);
    }

    private static void addProjectSite(Schema schema){
        Entity projectSite = schema.addEntity("ProjectSite");
        makeParcelable(projectSite);
        entities.put("ProjectSite", projectSite);
        setEntityBase(projectSite);
        Property projectProperty = projectSite.addLongProperty("projectID").getProperty();
        projectSite.addToOne(entities.get("Project"), projectProperty);
        Property siteProperty = projectSite.addLongProperty("siteID").getProperty();
        projectSite.addToOne(entities.get("Site"), siteProperty);
        setMetaElement(schema, "ProjectSiteMeta", projectSite);
    }

    private static void addCoordinate(Schema schema){
        Entity coordinate = schema.addEntity("Coordinate");
        entities.put("Coordinate", coordinate);
        setEntityBase(coordinate);
        coordinate.addDoubleProperty("latitude");
        coordinate.addDoubleProperty("longitude");
        coordinate.addDoubleProperty("elevation");
        coordinate.addFloatProperty("accuracy");
        coordinate.addLongProperty("time");
        coordinate.addFloatProperty("speed");
        coordinate.addFloatProperty("bearing");
        coordinate.addStringProperty("featureType");
        Property location = coordinate.addLongProperty("locationID").getProperty();
        coordinate.addToOne(entities.get("Location"), location);
    }

    private static void addElement(Schema schema){
        Entity element = schema.addEntity("Element");
        entities.put("Element", element);
        setEntityBase(element);
        element.addStringProperty("scode");
        element.addStringProperty("scientificName");
        element.addStringProperty("commonName");
        setMetaElement(schema, "ElementMeta", element);
    }

    private static void addElementGroup(Schema schema){
        Entity elementGroup = schema.addEntity("ElementGroup");
        entities.put("ElementGroup", elementGroup);
        setEntityBase(elementGroup);
        elementGroup.addStringProperty("name").notNull();
        setMetaElement(schema, "ElementGroupMeta", elementGroup);
        setOwner(elementGroup);
        Property group = addGroupElement(schema);
        ToMany elements = elementGroup.addToMany(entities.get("GroupElement"), group);
        elements.setName("groupElements");
    }

    private static Property addGroupElement(Schema schema){
        Entity groupElement = schema.addEntity("GroupElement");
        entities.put("GroupElement", groupElement);
        setEntityBase(groupElement);
        groupElement.addStringProperty("flags");
        Property groupProperty = groupElement.addLongProperty("elementGroupID").getProperty();
        groupElement.addToOne(entities.get("ElementGroup"), groupProperty);
        Property elementProperty = groupElement.addLongProperty("elementID").getProperty();
        groupElement.addToOne(entities.get("Element"), elementProperty);
        return groupProperty;
    }

    private static void addStation(Schema schema){
        Entity station = schema.addEntity("Station");
        entities.put("Station", station);
        setEntityBase(station);
        station.addStringProperty("name").notNull();
        station.addDateProperty("surveyDate");
        station.addDateProperty("surveyTime");
        station.addStringProperty("details");
        station.addStringProperty("description");
        station.addStringProperty("stationType").notNull();
        station.addStringProperty("timeZone");

        Property location = station.addLongProperty("locationID").getProperty();
        station.addToOne(entities.get("Location"), location);
        Property root = station.addLongProperty("rootID").getProperty();
        station.addToOne(station, root);
        Property projectSite = station.addLongProperty("projectSiteID").getProperty();
        station.addToOne(entities.get("ProjectSite"), projectSite);

        setMetaElement(schema,  "StationMeta", station);
    }

    private static void addStationElement(Schema schema){
        Entity stationElement = schema.addEntity("StationElement");
        entities.put("StationElement", stationElement);
        setEntityBase(stationElement);
        stationElement.addStringProperty("count");
        Property station = stationElement.addLongProperty("stationID").notNull().getProperty();
        stationElement.addToOne(entities.get("Station"), station);
        Property element = stationElement.addLongProperty("elementID").notNull().getProperty();
        stationElement.addToOne(entities.get("Element"), element);
        setMetaElement(schema, "StationElementMeta", stationElement);
    }

    private static void addObservationType(Schema schema){
        Entity observationType = schema.addEntity("ObservationType");
        entities.put("ObservationType", observationType);
        setEntityBase(observationType);
        makeParcelable(observationType);
        observationType.addStringProperty("name").notNull().unique();
        Property root = observationType.addLongProperty("rootID").getProperty();
        observationType.addToOne(observationType, root);
    }

    private static void addObservationGroup(Schema schema){
        Entity observationGroup = schema.addEntity("ObservationGroup");
        entities.put("ObservationGroup", observationGroup);
        setEntityBase(observationGroup);
        // makeParcelable(observationGroup);
        observationGroup.addStringProperty("name").notNull();
        setOwner(observationGroup);
        setMetaElement(schema, "ObservationGroupMeta", observationGroup);
        Property observations = addGroupObservation(schema);
        ToMany groupObservations = observationGroup.addToMany(entities.get("GroupObservation"), observations);
        groupObservations.setName("groupObservations");
    }

    private static Property addGroupObservation(Schema schema){
        Entity groupObservation = schema.addEntity("GroupObservation");
        entities.put("GroupObservation", groupObservation);
        setEntityBase(groupObservation);
        makeParcelable(groupObservation);
        groupObservation.addStringProperty("allowableValues");
        Property obType = groupObservation.addLongProperty("observationTypeID").notNull().getProperty();
        groupObservation.addToOne(entities.get("ObservationType"), obType);
        Property group = groupObservation.addLongProperty("observationGroupID").notNull().getProperty();
        groupObservation.addToOne(entities.get("ObservationGroup"), group);
        setMetaElement(schema, "GroupObservationMeta", groupObservation);
        return group;
    }

    private static void addObservation(Schema schema){
        Entity observation = schema.addEntity("Observation");
        entities.put("Observation", observation);
        setEntityBase(observation);
        observation.addStringProperty("observed");
        observation.addDateProperty("dateObservered");
        observation.addDateProperty("timeObserved");
        Property station = observation.addLongProperty("stationID").notNull().getProperty();
        observation.addToOne(entities.get("Station"), station);
        Property type = observation.addLongProperty("observationTypeID").notNull().getProperty();
        observation.addToOne(entities.get("ObservationType"), type);

        setMetaElement(schema, "ObservationMeta", observation);
    }

    private static void addParameter(Schema schema){
        Entity parameter = schema.addEntity("Parameter");
        entities.put("Parameter", parameter);
        setEntityBase(parameter);
        parameter.addStringProperty("name").notNull().unique();
    }

    private static void addMeasurement(Schema schema){
        Entity measurement = schema.addEntity("Measurement");
        entities.put("Measurement", measurement);
        setEntityBase(measurement);
        measurement.addDateProperty("dateMeasured");
        measurement.addDateProperty("timeMeasured");
        measurement.addStringProperty("matrix");
        measurement.addStringProperty("unit");
        measurement.addStringProperty("qualifier");
        measurement.addStringProperty("measurementText");
        Property station = measurement.addLongProperty("stationID").notNull().notNull().getProperty();
        measurement.addToOne(entities.get("Station"), station);
        Property parameter = measurement.addLongProperty("parameterID").notNull().getProperty();
        measurement.addToOne(entities.get("Parameter"), parameter);
        setMetaElement(schema, "MeasurementMeta", measurement);
    }

    private static void addPhotos(Schema schema){
        Entity photos = schema.addEntity("Photo");
        entities.put("Photo", photos);
        setEntityBase(photos);
        photos.addStringProperty("path");
        photos.addDateProperty("dateCollected");
        photos.addStringProperty("description");
        photos.addStringProperty("name");
        photos.addStringProperty("parentTable");
        photos.addLongProperty("parentID");
        Property coordinate = photos.addLongProperty("coordinateID").getProperty();
        photos.addToOne(entities.get("Coordinate"), coordinate);
        setMetaElement(schema, "PhotoMeta", photos);
    }
}
