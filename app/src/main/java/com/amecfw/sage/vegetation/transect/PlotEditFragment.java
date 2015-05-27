package com.amecfw.sage.vegetation.transect;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.amecfw.sage.fieldbook.StationEditFragmentBase;
import com.amecfw.sage.model.FieldDescriptor;
import com.amecfw.sage.model.ObservationDescriptor;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.vegetation.VegetationGlobals;

import java.util.Date;

/**
 * Created by amec on 2015-05-27.
 */
public class PlotEditFragment extends StationEditFragmentBase<PlotEditFragment.ViewModel> {


    @Override
    protected ViewModel createViewModel(StationProxy stationProxy) {
        return null;
    }

    @Override
    public ViewModel getViewModel() {
        return null;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {

    }

    public static class ViewModel implements com.amecfw.sage.proxy.ViewModel {

        @FieldDescriptor(clazz = Station.class, targetGetter = "getName", targetSetter = "setName")
        public String stationName;
        @ObservationDescriptor(fieldName="ecoSite", observationType = "eco site", defaultValue = "")
        public String ecoSite;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getStationType", targetSetter = "setStationType")
        public String stationType = VegetationGlobals.STATION_TYPE_VEGETATION_PLOT;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getSurveyDate", targetSetter = "setSurveyDate", type= DescriptorServices.TYPE_DATE)
        public Date dateCreated;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getSurveyTime", targetSetter = "setSurveyTime", type=DescriptorServices.TYPE_DATE)
        public Date timeCreated;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getTimeZone", targetSetter = "setTimeZone", type=DescriptorServices.TYPE_STRING)
        public String timeZone;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getDescription", targetSetter = "setDescription")
        public String comments;
        public String[] photos;
        public android.location.Location location;

        public static final Parcelable.Creator<ViewModel> CREATOR =
                new Parcelable.Creator<ViewModel>() {
                    @Override
                    public ViewModel createFromParcel(Parcel in) {return new ViewModel(in); }
                    @Override
                    public ViewModel[] newArray(int size) {return new ViewModel[size]; }
                };

        public ViewModel(){}

        public ViewModel(Parcel in){
            this.stationName = in.readString();
            this.ecoSite = in.readString();
            this.dateCreated = new Date(in.readLong());
            this.timeCreated = new Date(in.readLong());
            this.timeZone = in.readString();
            this.comments = in.readString();
            photos = new String[in.readInt()];
            in.readStringArray(photos);
            this.location = in.readParcelable(Location.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            dest.writeString(stationName);
            dest.writeString(ecoSite);
            dest.writeLong(dateCreated.getTime());
            dest.writeLong(timeCreated.getTime());
            dest.writeString(timeZone);
            dest.writeString(comments);
            dest.writeInt(photos == null ? 0 : photos.length );
            dest.writeStringArray(photos);
            dest.writeParcelable(location, PARCELABLE_WRITE_RETURN_VALUE);
        }

        @Override
        public int describeContents(){ return 0; }
    }
}
