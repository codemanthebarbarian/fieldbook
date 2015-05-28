package com.amecfw.sage.vegetation.transect;

import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.amecfw.sage.fieldbook.R;
import com.amecfw.sage.fieldbook.StationEditFragmentBase;
import com.amecfw.sage.model.FieldDescriptor;
import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.model.ObservationDescriptor;
import com.amecfw.sage.model.SageApplication;
import com.amecfw.sage.model.Station;
import com.amecfw.sage.model.service.DescriptorServices;
import com.amecfw.sage.model.service.StationService;
import com.amecfw.sage.proxy.StationProxy;
import com.amecfw.sage.ui.ObservationDialogFragment;
import com.amecfw.sage.util.Convert;
import com.amecfw.sage.util.OnExitListener;
import com.amecfw.sage.util.ViewState;
import com.amecfw.sage.vegetation.VegetationGlobals;

import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class PlotEditFragment extends StationEditFragmentBase<PlotEditFragment.ViewModel> {

    private ImageButton ecoSite;
    private EditText ecoSiteTextField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        notify = false;
        View view = inflater.inflate(R.layout.veg_trasect_plot_edit, container, false);
        super.initializeBase(view);
        ecoSiteTextField = (EditText)view.findViewById(R.id.plotEdit_layout_ecoSite);
        ecoSiteTextField.addTextChangedListener(textWatcher);
        ecoSite = (ImageButton) view.findViewById(R.id.plotEdit_layout_ecoSiteImageButton);
        ecoSite.setOnClickListener(ecoSiteListener);
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
        viewModel.ecoSite = Convert.toStringOrNull(ecoSiteTextField);
        viewModel.dateCreated = dateCreatedStamp;
        viewModel.timeCreated = timeCreatedStamp;
        viewModel.comments = Convert.toStringOrNull(comments);
        viewModel.timeZone = timeZone.getID();
        viewModel.location = location;
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
        ecoSiteTextField.setText(viewModel.ecoSite);
        dateCreatedStamp = viewModel.dateCreated;
        timeCreatedStamp = viewModel.timeCreated;
        comments.setText(viewModel.comments);
        if(viewModel.timeZone != null) timeZone = TimeZone.getTimeZone(viewModel.timeZone);
        setDateTimeCollected();
        location = viewModel.location;
        updateLocationText(location);
        mIsDirty = false;
        notify = true;
    }

    private View.OnClickListener ecoSiteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GroupObservation g = new GroupObservation();
            g.setAllowableValues("a1,b1,c1,c2,wetland 1, wetland 2");
            Bundle bundle = new Bundle();
            bundle.putParcelable(ObservationDialogFragment.ARG_GROUP_OBSERVATION, g);
            ObservationDialogFragment dialog = new ObservationDialogFragment();
            dialog.setArguments(bundle);
            dialog.setExitListener(new OnExitListener<String>() {
                @Override
                public void onExit(String viewModel, ViewState viewState) {
                    ecoSiteTextField.setText(viewModel);
                }
            });
            dialog.show(getFragmentManager(), null);
        }
    };

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
