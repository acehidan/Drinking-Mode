package dev.pranav.applock.features.appintro.ui

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import dev.pranav.applock.R
import dev.pranav.applock.core.utils.appLockRepository
import dev.pranav.applock.data.repository.AppLockRepository
import dev.pranav.applock.features.applist.domain.AppSearchManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppSelectionPage(
    appLockRepository: AppLockRepository = LocalContext.current.appLockRepository()
) {
    val context = LocalContext.current
    val appSearchManager = remember { AppSearchManager(context) }
    var apps by remember { mutableStateOf(emptySet<ApplicationInfo>()) }
    var selectedApps by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(key1 = Unit) {
        apps = withContext(Dispatchers.IO) {
            appSearchManager.loadApps()
        }
        selectedApps = appLockRepository.getLockedApps()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)), // Beige background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.lets_build_safe_zone),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C), // Dark Green
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.restrict_apps_description),
            fontSize = 16.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)
        ) {
            items(apps.toList(), key = { it.packageName }) { appInfo ->
                AppItem(
                    appInfo = appInfo,
                    isSelected = selectedApps.contains(appInfo.packageName),
                    onCheckedChange = { isChecked ->
                        val packageName = appInfo.packageName
                        if (isChecked) {
                            appLockRepository.addLockedApp(packageName)
                            selectedApps = selectedApps + packageName
                        } else {
                            appLockRepository.removeLockedApp(packageName)
                            selectedApps = selectedApps - packageName
                        }
                    }
                )
            }
        }
    }
}


@Composable
private fun AppItem(
    appInfo: ApplicationInfo,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val appName = remember(appInfo) { appInfo.loadLabel(packageManager).toString() }
    val iconBitmap = remember(appInfo) { appInfo.loadIcon(packageManager)?.toBitmap()?.asImageBitmap() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = if (iconBitmap != null) BitmapPainter(iconBitmap) else painterResource(id = R.drawable.ic_notification),
            contentDescription = appName,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = appName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.most_people_restrict_this_app),
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF388E3C), // Dark green
                uncheckedColor = Color.Gray
            )
        )
    }
}
