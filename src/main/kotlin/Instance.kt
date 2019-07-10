import org.dicthub.com_collinsdictionary.CollinsDictionaryProvider
import org.dicthub.plugin.shared.util.AjaxHttpClient

@JsName("create_plugin_com_collinsdictionary")
fun create_plugin_com_collinsdictionary(): CollinsDictionaryProvider {

    return CollinsDictionaryProvider(AjaxHttpClient)
}