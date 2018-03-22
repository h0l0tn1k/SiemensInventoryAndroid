package android.inventory.siemens.cz.siemensinventory.adapters

import android.content.Context
import android.inventory.siemens.cz.siemensinventory.R
import android.inventory.siemens.cz.siemensinventory.entity.KeyValueParameters
import android.inventory.siemens.cz.siemensinventory.entity.Permission
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by Stefan on 06-Mar-18.
 */
class DeviceParametersAdapter(
        private val context: Context,
        private val parameters: List<KeyValueParameters>
) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val parameter = getItem(position)

        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.device_params_item, parent, false)
        }

        val parameterNameView = view?.findViewById(R.id.parameterName) as TextView
        parameterNameView.text = parameter.key

        //TODO: read only mode
        val parameterValueReadonly = view?.findViewById(R.id.parameterValueReadonly) as TextView
        parameterValueReadonly.text = parameter.value

        //TODO: if edit mode
//        val parameterValueView = view?.findViewById(R.id.parameterValueEdit) as EditText
//        parameterValueView.setText(parameter.value)

        return view
    }

    override fun getItem(position: Int): KeyValueParameters = parameters[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = parameters.size

}