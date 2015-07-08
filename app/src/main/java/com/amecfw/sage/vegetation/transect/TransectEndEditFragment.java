package com.amecfw.sage.vegetation.transect;

import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.fieldbook.StationEditFragmentBase;
import com.amecfw.sage.model.FieldDescriptor;
import com.amecfw.sage.model.ObservationDescriptor;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.vegetation.VegetationGlobals;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class TransectEndEditFragment extends StationEditFragmentBase<TransectEndEditFragment.ViewModel> {

    android.location.Location tranLoc;
    private Long transID;

    private EditText tranDir;
    private EditText tranLength;

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        notify = false;
        View view = inflater.inflate(R.layout.veg_transect_end_edit, container, false);
        tranDir = (EditText) view.findViewById(R.id.transectEndEdit_layout_direction);
        tranLength = (EditText) view.findViewById(R.id.transectEndEdit_layout_length);
        super.initializeBase(view);
        initialize(savedInstanceState == null ? getArguments() : savedInstanceState);
        notify = true;
        return view;
    }

    @Override
    protected ViewModel createViewModel(StationProxy stationProxy) {
        ViewModel vm = new ViewModel();
        if(stationProxy == null) return vm;
        new StationService(SageApplication.getInstance().getDaoSession()).updateFromProxy(stationProxy, vm);
        vm.location = stationProxy.getGpsLocation();
        return vm;
    }

    @Override
    public ViewModel getViewModel() {
        ViewModel viewModel = new ViewModel();
        viewModel.stationName = Convert.toStringOrNull(stationName);
        viewModel.dateCreated = dateCreatedStamp;
        viewModel.timeCreated = timeCreatedStamp;
        viewModel.comments = Convert.toStringOrNull(comments);
        viewModel.timeZone = timeZone.getID();
        viewModel.location = location;
        viewModel.length = Convert.toStringOrNull(tranLength);
        viewModel.direction = Convert.toStringOrNull(tranDir);
        viewModel.transectId = transID;
        return viewModel;
    }

    @Override
    public void setViewModel(ViewModel viewModel) {
        notify = false;
        if(viewModel == null){
            notify = true;
            return;
        }
        stationName.setText(viewModel.stationName);
        if(viewModel.dateCreated == null){
            Calendar dt = Calendar.getInstance();
            if(dateCreatedStamp != null){
                dt.setTime(dateCreatedStamp);
                setDateCollected(dt);
            }
            if(timeCreatedStamp != null){
                dt.setTime(timeCreatedStamp);
                setTimeCollected(dt);
            }
        }else {
            dateCreatedStamp = viewModel.dateCreated;
            timeCreatedStamp = viewModel.timeCreated;
        }
        setDateTimeCollected();
        comments.setText(viewModel.comments);
        if(viewModel.timeZone != null) timeZone = TimeZone.getTimeZone(viewModel.timeZone);
        setDateTimeCollected();
        tranLoc = viewModel.transectLocation;
        location = viewModel.location;
        updateLocationText(location);
        transID = viewModel.transectId;
        updateLocationText(location);
        tranDir.setText(viewModel.direction);
        tranLength.setText(viewModel.length);
        mIsDirty = false;
        notify = true;
    }

    @Override
    protected void updateLocationText(Location location){
        super.updateLocationText(location);
        if(tranLoc != null && location != null){
            tranDir.setText(Convert.toStringOrNull(tranLoc.bearingTo(location)));
            tranLength.setText(Convert.toStringOrNull(tranLoc.distanceTo(location)));
        }
    }

    public static class ViewModel implements com.amecfw.sage.proxy.ViewModel {

        @FieldDescriptor(clazz = Station.class, targetGetter = "getName", targetSetter = "setName")
        public String stationName;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getStationType", targetSetter = "setStationType")
        public String stationType = VegetationGlobals.SURVEY_TRANSECT_END;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getSurveyDate", targetSetter = "setSurveyDate", type= DescriptorServices.TYPE_DATE)
        public Date dateCreated;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getSurveyTime", targetSetter = "setSurveyTime", type=DescriptorServices.TYPE_DATE)
        public Date timeCreated;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getTimeZone", targetSetter = "setTimeZone", type=DescriptorServices.TYPE_STRING)
        public String timeZone;
        @FieldDescriptor(clazz = Station.class, targetGetter = "getDescription", targetSetter = "setDescription")
        public String comments;
        @ObservationDescriptor(fieldName="direction", observationType = "Direction", defaultValue = "not recorded")
        public String direction;
        @ObservationDescriptor(fieldName="length", observationType = "Length", defaultValue = "not recorded")
        public String length;
        public String[] photos;
        public android.location.Location location;
        public Long transectId;
        public android.location.Location transectLocation;

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
            this.dateCreated = new Date(in.readLong());
            this.timeCreated = new Date(in.readLong());
            this.timeZone = in.readString();
            this.comments = in.readString();
            this.direction = in.readString();
            this.length = in.readString();
            this.transectId = in.readLong();
            photos = new String[in.readInt()];
            in.readStringArray(photos);
            this.location = in.readParcelable(Location.class.getClassLoader());
            this.transectLocation = in.readParcelable(Location.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            dest.writeString(stationName);
            dest.writeLong(dateCreated.getTime());
            dest.writeLong(timeCreated.getTime());
            dest.writeString(timeZone);
            dest.writeString(comments);
            dest.writeString(direction);
            dest.writeString(length);
            dest.writeLong(transectId);
            dest.writeInt(photos == null ? 0 : photos.length);
            dest.writeStringArray(photos);
            dest.writeParcelable(location, PARCELABLE_WRITE_RETURN_VALUE);
            dest.writeParcelable(transectLocation, PARCELABLE_WRITE_RETURN_VALUE);
        }

        @Override
        public int describeContents(){ return 0; }
    }
}
