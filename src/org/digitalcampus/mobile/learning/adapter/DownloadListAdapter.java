/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.mobile.learning.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.activity.DownloadActivity;
import org.digitalcampus.mobile.learning.listener.InstallModuleListener;
import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.task.DownloadModuleTask;
import org.digitalcampus.mobile.learning.task.InstallDownloadedModulesTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DownloadListAdapter extends ArrayAdapter<Module> implements InstallModuleListener{

	public static final String TAG = DownloadListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<Module> moduleList;
	private ProgressDialog myProgress;
	private SharedPreferences prefs;

	public DownloadListAdapter(Activity context, ArrayList<Module> moduleList) {
		super(context, R.layout.module_list_row, moduleList);
		this.ctx = context;
		this.moduleList = moduleList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.module_download_row, parent, false);
	    Module m = moduleList.get(position);
	    rowView.setTag(m);
	    
	    TextView moduleTitle = (TextView) rowView.findViewById(R.id.module_title);
	    moduleTitle.setText(m.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
	    
	    Button actionBtn = (Button) rowView.findViewById(R.id.action_btn);
	    
	    if(m.isInstalled()){
	    	if(m.isToUpdate()){
	    		actionBtn.setText(R.string.update);
		    	actionBtn.setEnabled(true);
	    	} else {
	    		actionBtn.setText(R.string.installed);
		    	actionBtn.setEnabled(false);
	    	}
	    } else {
	    	actionBtn.setText(R.string.install);
	    	actionBtn.setEnabled(true);
	    }
	    if(!m.isInstalled() || m.isToUpdate()){
	    	actionBtn.setTag(m);
	    	actionBtn.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		Module dm = (Module) v.getTag();
             		Log.d(TAG,dm.getDownloadUrl());
             		
             		ArrayList<Object> data = new ArrayList<Object>();
             		data.add(dm);
             		Payload p = new Payload(0,data);
             		
             		myProgress = new ProgressDialog(ctx);
             		myProgress.setTitle(R.string.install);
             		myProgress.setMessage(ctx.getString(R.string.download_starting));
             		myProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             		myProgress.setProgress(0);
             		myProgress.setMax(100);
             		myProgress.setCancelable(true);
             		myProgress.show();
                     
             		DownloadModuleTask dmt = new DownloadModuleTask(ctx);
             		dmt.setInstallerListener(DownloadListAdapter.this);
             		dmt.execute(p);
             	}
             });
	    }
	    return rowView;
	}

	public void downloadComplete() {
		myProgress.setMessage(ctx.getString(R.string.download_complete));
		// now set task to install
		InstallDownloadedModulesTask imTask = new InstallDownloadedModulesTask(ctx);
		imTask.setInstallerListener(DownloadListAdapter.this);
		imTask.execute();
	}

	public void installComplete() {
		Log.d(TAG,"install complete");
		myProgress.setProgress(100);
		myProgress.setMessage(ctx.getString(R.string.install_complete));
		myProgress.dismiss();
		// new refresh the module list
		DownloadActivity da = (DownloadActivity) ctx;
		da.refreshModuleList();
	}
	
	public void downloadProgressUpdate(DownloadProgress dp) {
		myProgress.setMessage(dp.getMessage());	
		myProgress.setProgress(dp.getProgress());
	}

	public void installProgressUpdate(DownloadProgress dp) {
		myProgress.setMessage(dp.getMessage());
		myProgress.setProgress(dp.getProgress());
	}
}
