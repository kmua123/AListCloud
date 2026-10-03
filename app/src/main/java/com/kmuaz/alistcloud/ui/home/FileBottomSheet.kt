package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kmuaz.alistcloud.data.network.model.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBottomSheet(

    file: FileItem,

    onDismiss: () -> Unit,

    onOpen: () -> Unit,

    onDownload: () -> Unit,

    onShare: () -> Unit,

    onProperty: () -> Unit

) {

    val sheetState =
        rememberModalBottomSheetState()

    ModalBottomSheet(

        onDismissRequest = onDismiss,

        sheetState = sheetState

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)

        ) {

            Text(

                text = file.name,

                style = MaterialTheme.typography.titleLarge

            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            MenuItem("📂 打开", onOpen)

            if (!file.is_dir) MenuItem("⬇ 下载", onDownload)

            if (!file.is_dir) MenuItem("🔗 分享", onShare)

            MenuItem("ℹ 属性", onProperty)

        }

    }

}
