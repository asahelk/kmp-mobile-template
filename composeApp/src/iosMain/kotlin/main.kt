import androidx.compose.ui.window.ComposeUIViewController
import org.taske.pokelist.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
