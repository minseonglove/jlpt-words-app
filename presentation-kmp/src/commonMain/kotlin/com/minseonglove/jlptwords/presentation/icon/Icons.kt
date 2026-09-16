package com.minseonglove.jlptwords.presentation.icon

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object Icons {
    val Ad: ImageVector
        get() {
            if (_ad != null) return _ad!!
            _ad =
                ImageVector
                    .Builder(
                        name = "Ad",
                        defaultWidth = 16.dp,
                        defaultHeight = 16.dp,
                        viewportWidth = 16f,
                        viewportHeight = 16f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(11.35f, 8.337f)
                            curveTo(11.35f, 7.638f, 10.93f, 7.199f, 10.349f, 7.199f)
                            curveTo(9.765f, 7.199f, 9.395f, 7.643f, 9.395f, 8.438f)
                            verticalLineTo(8.891f)
                            curveTo(9.395f, 9.691f, 9.769f, 10.139f, 10.367f, 10.139f)
                            curveTo(10.955f, 10.139f, 11.351f, 9.699f, 11.351f, 8.939f)
                            lineTo(11.35f, 8.337f)
                            close()
                            moveTo(5.937f, 8.574f)
                            lineTo(5.203f, 6.148f)
                            horizontalLineTo(5.15f)
                            lineTo(4.416f, 8.574f)
                            horizontalLineTo(5.937f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(2f, 2f)
                            curveTo(1.47f, 2f, 0.961f, 2.211f, 0.586f, 2.586f)
                            curveTo(0.211f, 2.961f, 0f, 3.47f, 0f, 4f)
                            lineTo(0f, 12f)
                            curveTo(0f, 12.53f, 0.211f, 13.039f, 0.586f, 13.414f)
                            curveTo(0.961f, 13.789f, 1.47f, 14f, 2f, 14f)
                            horizontalLineTo(14f)
                            curveTo(14.53f, 14f, 15.039f, 13.789f, 15.414f, 13.414f)
                            curveTo(15.789f, 13.039f, 16f, 12.53f, 16f, 12f)
                            verticalLineTo(4f)
                            curveTo(16f, 3.47f, 15.789f, 2.961f, 15.414f, 2.586f)
                            curveTo(15.039f, 2.211f, 14.53f, 2f, 14f, 2f)
                            horizontalLineTo(2f)
                            close()
                            moveTo(8.209f, 8.32f)
                            curveTo(8.209f, 7.04f, 8.903f, 6.276f, 9.962f, 6.276f)
                            curveTo(10.617f, 6.276f, 11.118f, 6.57f, 11.298f, 7.045f)
                            horizontalLineTo(11.351f)
                            verticalLineTo(4.685f)
                            horizontalLineTo(12.511f)
                            verticalLineTo(11f)
                            horizontalLineTo(11.373f)
                            verticalLineTo(10.253f)
                            horizontalLineTo(11.316f)
                            curveTo(11.171f, 10.727f, 10.626f, 11.057f, 9.949f, 11.057f)
                            curveTo(8.894f, 11.057f, 8.209f, 10.293f, 8.209f, 9.014f)
                            verticalLineTo(8.32f)
                            close()
                            moveTo(4.169f, 9.458f)
                            lineTo(3.7f, 11f)
                            horizontalLineTo(2.5f)
                            lineTo(4.513f, 5.001f)
                            horizontalLineTo(5.9f)
                            lineTo(7.905f, 11f)
                            horizontalLineTo(6.644f)
                            lineTo(6.174f, 9.458f)
                            horizontalLineTo(4.169f)
                            close()
                        }
                    }.build()
            return _ad!!
        }

    val Back: ImageVector
        get() {
            if (_back != null) return _back!!
            _back =
                ImageVector
                    .Builder(
                        name = "Back",
                        defaultWidth = 16.dp,
                        defaultHeight = 16.dp,
                        viewportWidth = 16f,
                        viewportHeight = 16f,
                    ).apply {
                        path(
                            fill = null,
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.5f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(10.25f, 3.5f)
                            lineTo(5.75f, 8f)
                            lineTo(10.25f, 12.5f)
                        }
                    }.build()
            return _back!!
        }

    val Check: ImageVector
        get() {
            if (_check != null) return _check!!
            _check =
                ImageVector
                    .Builder(
                        name = "Check",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(21.546f, 5.111f)
                            curveTo(21.827f, 5.392f, 21.985f, 5.774f, 21.985f, 6.171f)
                            curveTo(21.985f, 6.569f, 21.827f, 6.951f, 21.546f, 7.232f)
                            lineTo(10.303f, 18.475f)
                            curveTo(10.154f, 18.624f, 9.978f, 18.741f, 9.784f, 18.822f)
                            curveTo(9.59f, 18.902f, 9.382f, 18.944f, 9.172f, 18.944f)
                            curveTo(8.961f, 18.944f, 8.753f, 18.902f, 8.559f, 18.822f)
                            curveTo(8.365f, 18.741f, 8.189f, 18.624f, 8.04f, 18.475f)
                            lineTo(2.454f, 12.89f)
                            curveTo(2.311f, 12.752f, 2.196f, 12.586f, 2.118f, 12.403f)
                            curveTo(2.039f, 12.22f, 1.998f, 12.023f, 1.996f, 11.824f)
                            curveTo(1.994f, 11.625f, 2.032f, 11.427f, 2.108f, 11.243f)
                            curveTo(2.183f, 11.059f, 2.295f, 10.891f, 2.435f, 10.75f)
                            curveTo(2.576f, 10.609f, 2.744f, 10.498f, 2.928f, 10.423f)
                            curveTo(3.112f, 10.347f, 3.31f, 10.309f, 3.509f, 10.311f)
                            curveTo(3.708f, 10.313f, 3.905f, 10.354f, 4.088f, 10.433f)
                            curveTo(4.271f, 10.511f, 4.437f, 10.626f, 4.575f, 10.769f)
                            lineTo(9.171f, 15.365f)
                            lineTo(19.424f, 5.111f)
                            curveTo(19.563f, 4.972f, 19.729f, 4.861f, 19.911f, 4.786f)
                            curveTo(20.093f, 4.71f, 20.288f, 4.671f, 20.485f, 4.671f)
                            curveTo(20.682f, 4.671f, 20.877f, 4.71f, 21.059f, 4.786f)
                            curveTo(21.241f, 4.861f, 21.407f, 4.972f, 21.546f, 5.111f)
                            close()
                        }
                    }.build()
            return _check!!
        }

    val Circle: ImageVector
        get() {
            if (_circle != null) return _circle!!
            _circle =
                ImageVector
                    .Builder(
                        name = "Circle",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(4.5f, 12f)
                            curveTo(4.5f, 10.011f, 5.29f, 8.103f, 6.697f, 6.697f)
                            curveTo(8.103f, 5.29f, 10.011f, 4.5f, 12f, 4.5f)
                            curveTo(13.989f, 4.5f, 15.897f, 5.29f, 17.303f, 6.697f)
                            curveTo(18.71f, 8.103f, 19.5f, 10.011f, 19.5f, 12f)
                            curveTo(19.5f, 13.989f, 18.71f, 15.897f, 17.303f, 17.303f)
                            curveTo(15.897f, 18.71f, 13.989f, 19.5f, 12f, 19.5f)
                            curveTo(10.011f, 19.5f, 8.103f, 18.71f, 6.697f, 17.303f)
                            curveTo(5.29f, 15.897f, 4.5f, 13.989f, 4.5f, 12f)
                            close()
                            moveTo(12f, 5.5f)
                            curveTo(10.276f, 5.5f, 8.623f, 6.185f, 7.404f, 7.404f)
                            curveTo(6.185f, 8.623f, 5.5f, 10.276f, 5.5f, 12f)
                            curveTo(5.5f, 13.724f, 6.185f, 15.377f, 7.404f, 16.596f)
                            curveTo(8.623f, 17.815f, 10.276f, 18.5f, 12f, 18.5f)
                            curveTo(13.724f, 18.5f, 15.377f, 17.815f, 16.596f, 16.596f)
                            curveTo(17.815f, 15.377f, 18.5f, 13.724f, 18.5f, 12f)
                            curveTo(18.5f, 10.276f, 17.815f, 8.623f, 16.596f, 7.404f)
                            curveTo(15.377f, 6.185f, 13.724f, 5.5f, 12f, 5.5f)
                            close()
                        }
                    }.build()
            return _circle!!
        }

    val Play: ImageVector
        get() {
            if (_play != null) return _play!!
            _play =
                ImageVector
                    .Builder(
                        name = "Play",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(5.669f, 4.76f)
                            curveTo(5.696f, 4.533f, 5.776f, 4.315f, 5.901f, 4.124f)
                            curveTo(6.027f, 3.934f, 6.196f, 3.775f, 6.394f, 3.66f)
                            curveTo(6.592f, 3.546f, 6.814f, 3.48f, 7.042f, 3.466f)
                            curveTo(7.271f, 3.453f, 7.499f, 3.493f, 7.709f, 3.583f)
                            curveTo(8.771f, 4.037f, 11.151f, 5.116f, 14.171f, 6.859f)
                            curveTo(17.192f, 8.603f, 19.317f, 10.126f, 20.24f, 10.817f)
                            curveTo(21.028f, 11.408f, 21.03f, 12.58f, 20.241f, 13.173f)
                            curveTo(19.327f, 13.86f, 17.228f, 15.363f, 14.171f, 17.129f)
                            curveTo(11.111f, 18.895f, 8.759f, 19.961f, 7.707f, 20.409f)
                            curveTo(6.801f, 20.796f, 5.787f, 20.209f, 5.669f, 19.232f)
                            curveTo(5.531f, 18.09f, 5.273f, 15.497f, 5.273f, 11.995f)
                            curveTo(5.273f, 8.495f, 5.53f, 5.903f, 5.669f, 4.76f)
                            close()
                        }
                    }.build()
            return _play!!
        }

    val Fire: ImageVector
        get() {
            if (_fire != null) return _fire!!
            _fire =
                ImageVector
                    .Builder(
                        name = "Fire",
                        defaultWidth = 15.dp,
                        defaultHeight = 15.dp,
                        viewportWidth = 15f,
                        viewportHeight = 15f,
                    ).apply {
                        path(
                            fill =
                                Brush.linearGradient(
                                    0f to Color(0xFFFF9800),
                                    1f to Color(0xFFEE5A52),
                                    start =
                                        androidx.compose.ui.geometry
                                            .Offset(14.658f, 17.604f),
                                    end =
                                        androidx.compose.ui.geometry
                                            .Offset(-3.109f, 6.787f),
                                ),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(8.837f, 0.741f)
                            curveTo(8.686f, 0.626f, 8.482f, 0.607f, 8.312f, 0.691f)
                            curveTo(8.142f, 0.775f, 8.034f, 0.949f, 8.034f, 1.139f)
                            curveTo(8.034f, 2.893f, 7.616f, 5.202f, 5.483f, 6.116f)
                            curveTo(5.19f, 5.693f, 4.909f, 4.973f, 4.909f, 3.925f)
                            curveTo(4.909f, 3.751f, 4.819f, 3.59f, 4.671f, 3.499f)
                            curveTo(4.523f, 3.408f, 4.338f, 3.4f, 4.183f, 3.479f)
                            curveTo(3.095f, 4.03f, 1.618f, 5.597f, 1.618f, 8.568f)
                            curveTo(1.618f, 10.558f, 2.359f, 12.089f, 3.502f, 13.118f)
                            curveTo(4.636f, 14.139f, 6.134f, 14.639f, 7.618f, 14.639f)
                            curveTo(9.101f, 14.639f, 10.6f, 14.139f, 11.733f, 13.118f)
                            curveTo(12.876f, 12.089f, 13.618f, 10.558f, 13.618f, 8.568f)
                            curveTo(13.618f, 4.586f, 10.666f, 2.13f, 8.837f, 0.741f)
                            close()
                        }
                    }.build()
            return _fire!!
        }

    val Close: ImageVector
        get() {
            if (_close != null) return _close!!
            _close =
                ImageVector
                    .Builder(
                        name = "Close",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(12f, 13.4f)
                            lineTo(7.1f, 18.3f)
                            curveTo(6.917f, 18.483f, 6.683f, 18.575f, 6.4f, 18.575f)
                            curveTo(6.117f, 18.575f, 5.883f, 18.483f, 5.7f, 18.3f)
                            curveTo(5.517f, 18.117f, 5.425f, 17.883f, 5.425f, 17.6f)
                            curveTo(5.425f, 17.316f, 5.517f, 17.083f, 5.7f, 16.9f)
                            lineTo(10.6f, 12f)
                            lineTo(5.7f, 7.1f)
                            curveTo(5.517f, 6.916f, 5.425f, 6.683f, 5.425f, 6.4f)
                            curveTo(5.425f, 6.116f, 5.517f, 5.883f, 5.7f, 5.7f)
                            curveTo(5.883f, 5.516f, 6.117f, 5.425f, 6.4f, 5.425f)
                            curveTo(6.683f, 5.425f, 6.917f, 5.516f, 7.1f, 5.7f)
                            lineTo(12f, 10.6f)
                            lineTo(16.9f, 5.7f)
                            curveTo(17.083f, 5.516f, 17.317f, 5.425f, 17.6f, 5.425f)
                            curveTo(17.883f, 5.425f, 18.117f, 5.516f, 18.3f, 5.7f)
                            curveTo(18.483f, 5.883f, 18.575f, 6.116f, 18.575f, 6.4f)
                            curveTo(18.575f, 6.683f, 18.483f, 6.916f, 18.3f, 7.1f)
                            lineTo(13.4f, 12f)
                            lineTo(18.3f, 16.9f)
                            curveTo(18.483f, 17.083f, 18.575f, 17.316f, 18.575f, 17.6f)
                            curveTo(18.575f, 17.883f, 18.483f, 18.117f, 18.3f, 18.3f)
                            curveTo(18.117f, 18.483f, 17.883f, 18.575f, 17.6f, 18.575f)
                            curveTo(17.317f, 18.575f, 17.083f, 18.483f, 16.9f, 18.3f)
                            lineTo(12f, 13.4f)
                            close()
                        }
                    }.build()
            return _close!!
        }

    val Copy: ImageVector
        get() {
            if (_copy != null) return _copy!!
            _copy =
                ImageVector
                    .Builder(
                        name = "Copy",
                        defaultWidth = 16.dp,
                        defaultHeight = 16.dp,
                        viewportWidth = 16f,
                        viewportHeight = 16f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(0f, 6.75f)
                            curveTo(0f, 5.784f, 0.784f, 5f, 1.75f, 5f)
                            horizontalLineTo(3.25f)
                            curveTo(3.449f, 5f, 3.64f, 5.079f, 3.78f, 5.22f)
                            curveTo(3.921f, 5.36f, 4f, 5.551f, 4f, 5.75f)
                            curveTo(4f, 5.949f, 3.921f, 6.14f, 3.78f, 6.28f)
                            curveTo(3.64f, 6.421f, 3.449f, 6.5f, 3.25f, 6.5f)
                            horizontalLineTo(1.75f)
                            curveTo(1.684f, 6.5f, 1.62f, 6.526f, 1.573f, 6.573f)
                            curveTo(1.526f, 6.62f, 1.5f, 6.684f, 1.5f, 6.75f)
                            verticalLineTo(14.25f)
                            curveTo(1.5f, 14.388f, 1.612f, 14.5f, 1.75f, 14.5f)
                            horizontalLineTo(9.25f)
                            curveTo(9.316f, 14.5f, 9.38f, 14.474f, 9.427f, 14.427f)
                            curveTo(9.474f, 14.38f, 9.5f, 14.316f, 9.5f, 14.25f)
                            verticalLineTo(12.75f)
                            curveTo(9.5f, 12.551f, 9.579f, 12.36f, 9.72f, 12.22f)
                            curveTo(9.86f, 12.079f, 10.051f, 12f, 10.25f, 12f)
                            curveTo(10.449f, 12f, 10.64f, 12.079f, 10.78f, 12.22f)
                            curveTo(10.921f, 12.36f, 11f, 12.551f, 11f, 12.75f)
                            verticalLineTo(14.25f)
                            curveTo(11f, 14.714f, 10.816f, 15.159f, 10.487f, 15.487f)
                            curveTo(10.159f, 15.816f, 9.714f, 16f, 9.25f, 16f)
                            horizontalLineTo(1.75f)
                            curveTo(1.286f, 16f, 0.841f, 15.816f, 0.513f, 15.487f)
                            curveTo(0.184f, 15.159f, 0f, 14.714f, 0f, 14.25f)
                            lineTo(0f, 6.75f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(5f, 1.75f)
                            curveTo(5f, 0.784f, 5.784f, 0f, 6.75f, 0f)
                            horizontalLineTo(14.25f)
                            curveTo(15.216f, 0f, 16f, 0.784f, 16f, 1.75f)
                            verticalLineTo(9.25f)
                            curveTo(16f, 9.714f, 15.816f, 10.159f, 15.487f, 10.487f)
                            curveTo(15.159f, 10.816f, 14.714f, 11f, 14.25f, 11f)
                            horizontalLineTo(6.75f)
                            curveTo(6.286f, 11f, 5.841f, 10.816f, 5.513f, 10.487f)
                            curveTo(5.184f, 10.159f, 5f, 9.714f, 5f, 9.25f)
                            verticalLineTo(1.75f)
                            close()
                            moveTo(6.75f, 1.5f)
                            curveTo(6.684f, 1.5f, 6.62f, 1.526f, 6.573f, 1.573f)
                            curveTo(6.526f, 1.62f, 6.5f, 1.684f, 6.5f, 1.75f)
                            verticalLineTo(9.25f)
                            curveTo(6.5f, 9.388f, 6.612f, 9.5f, 6.75f, 9.5f)
                            horizontalLineTo(14.25f)
                            curveTo(14.316f, 9.5f, 14.38f, 9.474f, 14.427f, 9.427f)
                            curveTo(14.474f, 9.38f, 14.5f, 9.316f, 14.5f, 9.25f)
                            verticalLineTo(1.75f)
                            curveTo(14.5f, 1.684f, 14.474f, 1.62f, 14.427f, 1.573f)
                            curveTo(14.38f, 1.526f, 14.316f, 1.5f, 14.25f, 1.5f)
                            horizontalLineTo(6.75f)
                            close()
                        }
                    }.build()
            return _copy!!
        }

    val DropdownArrow: ImageVector
        get() {
            if (_dropdownArrow != null) return _dropdownArrow!!
            _dropdownArrow =
                ImageVector
                    .Builder(
                        name = "DropdownArrow",
                        defaultWidth = 16.dp,
                        defaultHeight = 16.dp,
                        viewportWidth = 16f,
                        viewportHeight = 16f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(7.247f, 11.14f)
                            lineTo(2.451f, 5.658f)
                            curveTo(1.885f, 5.013f, 2.345f, 4f, 3.204f, 4f)
                            horizontalLineTo(12.796f)
                            curveTo(12.988f, 4f, 13.177f, 4.055f, 13.338f, 4.159f)
                            curveTo(13.5f, 4.263f, 13.628f, 4.412f, 13.708f, 4.587f)
                            curveTo(13.787f, 4.762f, 13.814f, 4.956f, 13.786f, 5.146f)
                            curveTo(13.758f, 5.336f, 13.676f, 5.514f, 13.549f, 5.659f)
                            lineTo(8.753f, 11.139f)
                            curveTo(8.659f, 11.246f, 8.543f, 11.333f, 8.414f, 11.392f)
                            curveTo(8.284f, 11.45f, 8.143f, 11.481f, 8f, 11.481f)
                            curveTo(7.857f, 11.481f, 7.716f, 11.45f, 7.587f, 11.392f)
                            curveTo(7.457f, 11.333f, 7.341f, 11.246f, 7.247f, 11.139f)
                            verticalLineTo(11.14f)
                            close()
                        }
                    }.build()
            return _dropdownArrow!!
        }

    val Pass: ImageVector
        get() {
            if (_pass != null) return _pass!!
            _pass =
                ImageVector
                    .Builder(
                        name = "Pass",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 2f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(2f, 20.418f)
                            curveTo(4.447f, 17.431f, 6.619f, 15.737f, 8.518f, 15.334f)
                            curveTo(10.417f, 14.931f, 12.224f, 14.87f, 13.941f, 15.151f)
                            verticalLineTo(20.5f)
                            lineTo(22f, 11.773f)
                            lineTo(13.941f, 3.5f)
                            verticalLineTo(8.583f)
                            curveTo(10.767f, 8.608f, 8.068f, 9.747f, 5.845f, 12f)
                            curveTo(3.623f, 14.253f, 2.341f, 17.059f, 2f, 20.418f)
                            close()
                        }
                    }.build()
            return _pass!!
        }

    val Timer: ImageVector
        get() {
            if (_timer != null) return _timer!!
            _timer =
                ImageVector
                    .Builder(
                        name = "Timer",
                        defaultWidth = 15.dp,
                        defaultHeight = 15.dp,
                        viewportWidth = 15f,
                        viewportHeight = 15f,
                    ).apply {
                        path(
                            fill = SolidColor(Color(0xFF764ba2)),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(7.539f, 3.245f)
                            curveTo(7.738f, 3.245f, 7.928f, 3.324f, 8.069f, 3.465f)
                            curveTo(8.21f, 3.606f, 8.289f, 3.796f, 8.289f, 3.995f)
                            verticalLineTo(7.57f)
                            lineTo(10.425f, 8.852f)
                            curveTo(10.51f, 8.903f, 10.585f, 8.969f, 10.644f, 9.048f)
                            curveTo(10.704f, 9.128f, 10.747f, 9.218f, 10.772f, 9.314f)
                            curveTo(10.796f, 9.41f, 10.802f, 9.51f, 10.787f, 9.608f)
                            curveTo(10.773f, 9.706f, 10.739f, 9.8f, 10.688f, 9.885f)
                            curveTo(10.637f, 9.97f, 10.57f, 10.044f, 10.49f, 10.103f)
                            curveTo(10.41f, 10.162f, 10.319f, 10.204f, 10.223f, 10.228f)
                            curveTo(10.127f, 10.251f, 10.027f, 10.255f, 9.929f, 10.24f)
                            curveTo(9.831f, 10.225f, 9.737f, 10.19f, 9.653f, 10.138f)
                            lineTo(7.153f, 8.638f)
                            curveTo(7.042f, 8.572f, 6.95f, 8.477f, 6.886f, 8.365f)
                            curveTo(6.822f, 8.252f, 6.789f, 8.125f, 6.789f, 7.995f)
                            verticalLineTo(3.995f)
                            curveTo(6.789f, 3.796f, 6.868f, 3.606f, 7.008f, 3.465f)
                            curveTo(7.149f, 3.324f, 7.34f, 3.245f, 7.539f, 3.245f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color(0xFF667EEA)),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(2.039f, 7.495f)
                            curveTo(2.039f, 6.352f, 2.395f, 5.237f, 3.057f, 4.306f)
                            curveTo(3.72f, 3.374f, 4.657f, 2.672f, 5.737f, 2.298f)
                            curveTo(6.817f, 1.923f, 7.987f, 1.895f, 9.084f, 2.216f)
                            curveTo(10.181f, 2.537f, 11.151f, 3.192f, 11.859f, 4.09f)
                            lineTo(10.893f, 5.055f)
                            curveTo(10.823f, 5.125f, 10.775f, 5.214f, 10.755f, 5.311f)
                            curveTo(10.736f, 5.408f, 10.746f, 5.509f, 10.784f, 5.6f)
                            curveTo(10.821f, 5.692f, 10.886f, 5.77f, 10.968f, 5.825f)
                            curveTo(11.05f, 5.88f, 11.147f, 5.909f, 11.246f, 5.909f)
                            horizontalLineTo(14.039f)
                            curveTo(14.171f, 5.909f, 14.299f, 5.857f, 14.392f, 5.763f)
                            curveTo(14.486f, 5.669f, 14.539f, 5.542f, 14.539f, 5.409f)
                            verticalLineTo(2.615f)
                            curveTo(14.539f, 2.516f, 14.51f, 2.419f, 14.455f, 2.337f)
                            curveTo(14.4f, 2.255f, 14.322f, 2.19f, 14.23f, 2.153f)
                            curveTo(14.139f, 2.115f, 14.038f, 2.105f, 13.941f, 2.124f)
                            curveTo(13.844f, 2.143f, 13.755f, 2.191f, 13.685f, 2.261f)
                            lineTo(12.925f, 3.022f)
                            curveTo(11.917f, 1.809f, 10.529f, 0.972f, 8.985f, 0.646f)
                            curveTo(7.442f, 0.32f, 5.834f, 0.526f, 4.422f, 1.228f)
                            curveTo(3.009f, 1.931f, 1.876f, 3.09f, 1.205f, 4.518f)
                            curveTo(0.534f, 5.945f, 0.365f, 7.557f, 0.725f, 9.093f)
                            curveTo(1.085f, 10.629f, 1.953f, 11.998f, 3.189f, 12.979f)
                            curveTo(4.424f, 13.959f, 5.955f, 14.493f, 7.532f, 14.495f)
                            curveTo(9.11f, 14.497f, 10.641f, 13.966f, 11.879f, 12.988f)
                            curveTo(13.117f, 12.01f, 13.988f, 10.643f, 14.352f, 9.108f)
                            curveTo(14.396f, 8.915f, 14.362f, 8.712f, 14.257f, 8.544f)
                            curveTo(14.153f, 8.376f, 13.986f, 8.256f, 13.793f, 8.21f)
                            curveTo(13.6f, 8.165f, 13.397f, 8.198f, 13.228f, 8.301f)
                            curveTo(13.059f, 8.405f, 12.938f, 8.572f, 12.892f, 8.764f)
                            curveTo(12.582f, 10.077f, 11.8f, 11.231f, 10.696f, 12.005f)
                            curveTo(9.591f, 12.78f, 8.24f, 13.121f, 6.9f, 12.964f)
                            curveTo(5.56f, 12.807f, 4.324f, 12.164f, 3.428f, 11.155f)
                            curveTo(2.532f, 10.147f, 2.037f, 8.844f, 2.039f, 7.495f)
                            close()
                        }
                    }.build()
            return _timer!!
        }

    val FireFreeze: ImageVector
        get() {
            if (_fireFreeze != null) return _fireFreeze!!
            _fireFreeze =
                ImageVector
                    .Builder(
                        name = "FireFreeze",
                        defaultWidth = 14.dp,
                        defaultHeight = 14.dp,
                        viewportWidth = 14f,
                        viewportHeight = 14f,
                    ).apply {
                        path(
                            fill = SolidColor(Color(0xFF667EEA)),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(8.177f, 0.476f)
                            curveTo(8.025f, 0.362f, 7.823f, 0.344f, 7.653f, 0.429f)
                            curveTo(7.484f, 0.514f, 7.377f, 0.687f, 7.377f, 0.876f)
                            curveTo(7.377f, 2.521f, 6.981f, 4.673f, 4.971f, 5.533f)
                            curveTo(4.699f, 5.138f, 4.439f, 4.47f, 4.439f, 3.501f)
                            curveTo(4.439f, 3.328f, 4.35f, 3.167f, 4.202f, 3.076f)
                            curveTo(4.055f, 2.985f, 3.871f, 2.976f, 3.716f, 3.054f)
                            curveTo(2.67f, 3.577f, 1.252f, 5.063f, 1.252f, 7.876f)
                            curveTo(1.252f, 9.762f, 1.964f, 11.213f, 3.06f, 12.187f)
                            curveTo(4.148f, 13.154f, 5.583f, 13.626f, 7.002f, 13.626f)
                            curveTo(8.421f, 13.626f, 9.856f, 13.154f, 10.943f, 12.187f)
                            curveTo(12.04f, 11.213f, 12.752f, 9.762f, 12.752f, 7.876f)
                            curveTo(12.752f, 4.106f, 9.921f, 1.784f, 8.177f, 0.476f)
                            close()
                        }
                    }.build()
            return _fireFreeze!!
        }

    val AdZero: ImageVector
        get() {
            if (_adZero != null) return _adZero!!
            _adZero =
                ImageVector
                    .Builder(
                        name = "AdZero",
                        defaultWidth = 24.dp,
                        defaultHeight = 24.dp,
                        viewportWidth = 24f,
                        viewportHeight = 24f,
                    ).apply {
                        path(
                            fill = null,
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 2f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(4.91f, 4.949f)
                            curveTo(3.044f, 6.821f, 1.997f, 9.357f, 2f, 12f)
                            curveTo(2f, 17.523f, 6.477f, 22f, 12f, 22f)
                            curveTo(14.643f, 22.003f, 17.178f, 20.957f, 19.05f, 19.091f)
                            moveTo(20.778f, 16.793f)
                            curveTo(21.582f, 15.323f, 22.002f, 13.675f, 22f, 12f)
                            curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
                            curveTo(10.26f, 2f, 8.624f, 2.444f, 7.2f, 3.225f)
                        }
                        path(
                            fill = null,
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 2f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(7f, 15f)
                            verticalLineTo(10.5f)
                            curveTo(7f, 10.247f, 7.064f, 9.999f, 7.185f, 9.777f)
                            curveTo(7.307f, 9.556f, 7.483f, 9.369f, 7.696f, 9.233f)
                            curveTo(7.909f, 9.098f, 8.153f, 9.019f, 8.405f, 9.003f)
                            curveTo(8.657f, 8.987f, 8.909f, 9.035f, 9.138f, 9.142f)
                            moveTo(9.854f, 9.853f)
                            curveTo(9.948f, 10.049f, 10f, 10.268f, 10f, 10.5f)
                            verticalLineTo(15f)
                            moveTo(7f, 13f)
                            horizontalLineTo(10f)
                            moveTo(14f, 14f)
                            verticalLineTo(15f)
                            horizontalLineTo(15f)
                            moveTo(17f, 13f)
                            verticalLineTo(11f)
                            curveTo(17f, 10.47f, 16.789f, 9.961f, 16.414f, 9.586f)
                            curveTo(16.039f, 9.211f, 15.53f, 9f, 15f, 9f)
                            horizontalLineTo(14f)
                            verticalLineTo(10f)
                            moveTo(3f, 3f)
                            lineTo(21f, 21f)
                        }
                    }.build()
            return _adZero!!
        }

    val Rocket: ImageVector
        get() {
            if (_rocket != null) return _rocket!!
            _rocket =
                ImageVector
                    .Builder(
                        name = "Rocket",
                        defaultWidth = 14.dp,
                        defaultHeight = 14.dp,
                        viewportWidth = 14f,
                        viewportHeight = 14f,
                    ).apply {
                        path(
                            fill =
                                Brush.linearGradient(
                                    0f to Color(0xFFFFD600),
                                    1f to Color(0xFF00D078),
                                    start =
                                        androidx.compose.ui.geometry
                                            .Offset(2.289f, 2.692f),
                                    end =
                                        androidx.compose.ui.geometry
                                            .Offset(13.596f, 8.957f),
                                ),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(6.536f, 10.283f)
                            lineTo(3.722f, 7.468f)
                            curveTo(4.031f, 6.949f, 4.339f, 6.413f, 4.645f, 5.881f)
                            curveTo(5.663f, 4.111f, 6.657f, 2.384f, 7.587f, 1.485f)
                            curveTo(10.115f, -1.044f, 13.577f, 0.423f, 13.577f, 0.423f)
                            curveTo(13.577f, 0.423f, 15.043f, 3.887f, 12.515f, 6.416f)
                            curveTo(11.625f, 7.339f, 9.925f, 8.318f, 8.17f, 9.33f)
                            curveTo(7.623f, 9.645f, 7.071f, 9.963f, 6.536f, 10.283f)
                            close()
                            moveTo(8.68f, 3.738f)
                            curveTo(8.68f, 2.864f, 9.389f, 2.154f, 10.264f, 2.154f)
                            curveTo(11.138f, 2.154f, 11.847f, 2.864f, 11.847f, 3.738f)
                            curveTo(11.847f, 4.613f, 11.138f, 5.322f, 10.264f, 5.322f)
                            curveTo(9.389f, 5.322f, 8.68f, 4.613f, 8.68f, 3.738f)
                            close()
                            moveTo(4.583f, 3.506f)
                            curveTo(3.163f, 3.128f, 1.83f, 3.828f, 0.647f, 4.914f)
                            curveTo(0.41f, 5.131f, 0.46f, 5.512f, 0.735f, 5.678f)
                            lineTo(2.645f, 6.829f)
                            lineTo(2.647f, 6.825f)
                            curveTo(2.922f, 6.364f, 3.23f, 5.83f, 3.541f, 5.289f)
                            curveTo(3.897f, 4.671f, 4.259f, 4.044f, 4.583f, 3.506f)
                            close()
                            moveTo(7.174f, 11.361f)
                            lineTo(8.324f, 13.271f)
                            curveTo(8.49f, 13.547f, 8.871f, 13.596f, 9.089f, 13.359f)
                            curveTo(10.174f, 12.175f, 10.874f, 10.842f, 10.496f, 9.42f)
                            curveTo(9.974f, 9.735f, 9.425f, 10.052f, 8.881f, 10.365f)
                            lineTo(8.798f, 10.413f)
                            curveTo(8.248f, 10.73f, 7.705f, 11.043f, 7.179f, 11.358f)
                            lineTo(7.174f, 11.361f)
                            close()
                            moveTo(2.605f, 9.362f)
                            curveTo(2.874f, 9.357f, 3.141f, 9.407f, 3.391f, 9.508f)
                            curveTo(3.64f, 9.608f, 3.867f, 9.758f, 4.057f, 9.949f)
                            curveTo(4.247f, 10.139f, 4.397f, 10.365f, 4.497f, 10.615f)
                            curveTo(4.598f, 10.864f, 4.647f, 11.131f, 4.643f, 11.4f)
                            curveTo(4.638f, 11.669f, 4.579f, 11.934f, 4.47f, 12.18f)
                            curveTo(4.361f, 12.424f, 4.204f, 12.644f, 4.009f, 12.827f)
                            curveTo(3.787f, 13.04f, 3.439f, 13.206f, 3.117f, 13.334f)
                            curveTo(2.774f, 13.471f, 2.382f, 13.593f, 2.016f, 13.693f)
                            curveTo(1.649f, 13.793f, 1.298f, 13.874f, 1.033f, 13.927f)
                            curveTo(0.901f, 13.953f, 0.785f, 13.973f, 0.697f, 13.985f)
                            curveTo(0.654f, 13.991f, 0.607f, 13.996f, 0.563f, 13.998f)
                            curveTo(0.544f, 13.999f, 0.508f, 14f, 0.467f, 13.996f)
                            curveTo(0.448f, 13.994f, 0.41f, 13.99f, 0.365f, 13.976f)
                            curveTo(0.335f, 13.967f, 0.22f, 13.932f, 0.124f, 13.82f)
                            curveTo(0.042f, 13.724f, 0.019f, 13.624f, 0.014f, 13.6f)
                            curveTo(0.005f, 13.562f, 0.003f, 13.529f, 0.002f, 13.512f)
                            curveTo(-0f, 13.476f, 0.001f, 13.444f, 0.002f, 13.425f)
                            curveTo(0.005f, 13.384f, 0.011f, 13.338f, 0.017f, 13.296f)
                            curveTo(0.03f, 13.208f, 0.051f, 13.093f, 0.077f, 12.961f)
                            curveTo(0.131f, 12.697f, 0.212f, 12.348f, 0.313f, 11.982f)
                            curveTo(0.413f, 11.618f, 0.535f, 11.227f, 0.672f, 10.885f)
                            curveTo(0.8f, 10.564f, 0.967f, 10.218f, 1.179f, 9.996f)
                            curveTo(1.361f, 9.801f, 1.581f, 9.644f, 1.826f, 9.535f)
                            curveTo(2.071f, 9.426f, 2.337f, 9.367f, 2.605f, 9.362f)
                            close()
                        }
                    }.build()
            return _rocket!!
        }

    val Warning: ImageVector
        get() {
            if (_warning != null) return _warning!!
            _warning =
                ImageVector
                    .Builder(
                        name = "Warning",
                        defaultWidth = 16.dp,
                        defaultHeight = 16.dp,
                        viewportWidth = 16f,
                        viewportHeight = 16f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.EvenOdd,
                        ) {
                            moveTo(6.285f, 1.975f)
                            curveTo(7.06f, 0.68f, 8.939f, 0.68f, 9.715f, 1.975f)
                            lineTo(15.708f, 11.972f)
                            curveTo(16.507f, 13.305f, 15.547f, 15f, 13.992f, 15f)
                            horizontalLineTo(2.008f)
                            curveTo(0.453f, 15f, -0.507f, 13.305f, 0.292f, 11.972f)
                            lineTo(6.285f, 1.975f)
                            close()
                            moveTo(8f, 5f)
                            curveTo(8.199f, 5f, 8.39f, 5.079f, 8.53f, 5.22f)
                            curveTo(8.671f, 5.36f, 8.75f, 5.551f, 8.75f, 5.75f)
                            verticalLineTo(8.75f)
                            curveTo(8.75f, 8.949f, 8.671f, 9.14f, 8.53f, 9.28f)
                            curveTo(8.39f, 9.421f, 8.199f, 9.5f, 8f, 9.5f)
                            curveTo(7.801f, 9.5f, 7.61f, 9.421f, 7.47f, 9.28f)
                            curveTo(7.329f, 9.14f, 7.25f, 8.949f, 7.25f, 8.75f)
                            verticalLineTo(5.75f)
                            curveTo(7.25f, 5.551f, 7.329f, 5.36f, 7.47f, 5.22f)
                            curveTo(7.61f, 5.079f, 7.801f, 5f, 8f, 5f)
                            close()
                            moveTo(9f, 11.5f)
                            curveTo(9f, 11.765f, 8.895f, 12.019f, 8.707f, 12.207f)
                            curveTo(8.52f, 12.395f, 8.265f, 12.5f, 8f, 12.5f)
                            curveTo(7.735f, 12.5f, 7.48f, 12.395f, 7.293f, 12.207f)
                            curveTo(7.105f, 12.019f, 7f, 11.765f, 7f, 11.5f)
                            curveTo(7f, 11.235f, 7.105f, 10.98f, 7.293f, 10.793f)
                            curveTo(7.48f, 10.605f, 7.735f, 10.5f, 8f, 10.5f)
                            curveTo(8.265f, 10.5f, 8.52f, 10.605f, 8.707f, 10.793f)
                            curveTo(8.895f, 10.98f, 9f, 11.235f, 9f, 11.5f)
                            close()
                        }
                    }.build()
            return _warning!!
        }

    val More: ImageVector
        get() {
            if (_more != null) return _more!!
            _more =
                ImageVector
                    .Builder(
                        name = "More",
                        defaultWidth = 16.dp,
                        defaultHeight = 16.dp,
                        viewportWidth = 16f,
                        viewportHeight = 16f,
                    ).apply {
                        path(
                            fill = null,
                            fillAlpha = 1.0f,
                            stroke = SolidColor(Color.Black),
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.5f,
                            strokeLineCap = StrokeCap.Round,
                            strokeLineJoin = StrokeJoin.Round,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(2.75f, 12.25f)
                            horizontalLineTo(13.25f)
                            moveTo(2.75f, 8.25f)
                            horizontalLineTo(13.25f)
                            moveTo(2.75f, 4.25f)
                            horizontalLineTo(13.25f)
                        }
                    }.build()
            return _more!!
        }

    val Speaker: ImageVector
        get() {
            if (_speaker != null) return _speaker!!
            _speaker =
                ImageVector
                    .Builder(
                        name = "Speaker",
                        defaultWidth = 28.dp,
                        defaultHeight = 28.dp,
                        viewportWidth = 28f,
                        viewportHeight = 28f,
                    ).apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(13f, 24.726f)
                            curveTo(13.79f, 24.726f, 14.361f, 24.144f, 14.361f, 23.365f)
                            verticalLineTo(4.819f)
                            curveTo(14.361f, 4.04f, 13.79f, 3.389f, 12.977f, 3.389f)
                            curveTo(12.407f, 3.389f, 12.024f, 3.644f, 11.407f, 4.226f)
                            lineTo(6.279f, 9.075f)
                            curveTo(6.195f, 9.145f, 6.087f, 9.182f, 5.977f, 9.179f)
                            horizontalLineTo(2.523f)
                            curveTo(0.884f, 9.179f, 0f, 10.074f, 0f, 11.819f)
                            verticalLineTo(16.33f)
                            curveTo(0f, 18.075f, 0.883f, 18.97f, 2.523f, 18.97f)
                            horizontalLineTo(5.977f)
                            curveTo(6.093f, 18.97f, 6.198f, 19.005f, 6.279f, 19.075f)
                            lineTo(11.407f, 23.97f)
                            curveTo(11.965f, 24.493f, 12.43f, 24.726f, 13f, 24.726f)
                            close()
                            moveTo(24.244f, 22.354f)
                            curveTo(24.698f, 22.656f, 25.256f, 22.54f, 25.581f, 22.075f)
                            curveTo(27.117f, 19.935f, 28f, 17.086f, 28f, 14.11f)
                            curveTo(28f, 11.121f, 27.128f, 8.272f, 25.581f, 6.133f)
                            curveTo(25.244f, 5.679f, 24.698f, 5.563f, 24.244f, 5.865f)
                            curveTo(23.802f, 6.168f, 23.733f, 6.737f, 24.081f, 7.237f)
                            curveTo(25.349f, 9.098f, 26.128f, 11.54f, 26.128f, 14.109f)
                            curveTo(26.128f, 16.679f, 25.372f, 19.144f, 24.069f, 20.982f)
                            curveTo(23.744f, 21.482f, 23.802f, 22.052f, 24.244f, 22.354f)
                            close()
                            moveTo(19.604f, 19.214f)
                            curveTo(20f, 19.493f, 20.569f, 19.4f, 20.907f, 18.947f)
                            curveTo(21.814f, 17.726f, 22.361f, 15.935f, 22.361f, 14.109f)
                            curveTo(22.361f, 12.284f, 21.802f, 10.505f, 20.907f, 9.261f)
                            curveTo(20.57f, 8.807f, 20.012f, 8.714f, 19.604f, 8.993f)
                            curveTo(19.093f, 9.33f, 19.035f, 9.923f, 19.407f, 10.435f)
                            curveTo(20.081f, 11.342f, 20.489f, 12.726f, 20.489f, 14.11f)
                            curveTo(20.489f, 15.494f, 20.059f, 16.877f, 19.396f, 17.795f)
                            curveTo(19.046f, 18.295f, 19.104f, 18.865f, 19.604f, 19.214f)
                        }
                    }.build()
            return _speaker!!
        }

    /** 꼬리가 흐려지는 링. 정지 상태의 벡터이므로 사용처에서 회전시켜야 스피너가 된다. */
    val Loading: ImageVector
        get() {
            if (_loading != null) return _loading!!
            _loading =
                ImageVector
                    .Builder(
                        name = "Loading",
                        defaultWidth = 13.3333.dp,
                        defaultHeight = 13.3333.dp,
                        viewportWidth = 13.3333f,
                        viewportHeight = 13.3333f,
                    ).apply {
                        path(
                            fill =
                                Brush.linearGradient(
                                    0f to Color.Black,
                                    1f to Color.Black.copy(alpha = 0.55f),
                                    start = Offset(3.33333f, 0.700835f),
                                    end = Offset(3.33333f, 12.2048f),
                                ),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(5.924f, 0.004f)
                            curveTo(6.09975f, -0.0154f, 6.27603f, 0.03576f, 6.41406f, 0.14628f)
                            curveTo(6.55208f, 0.2568f, 6.64055f, 0.41763f, 6.66f, 0.59338f)
                            curveTo(6.67945f, 0.76913f, 6.62829f, 0.94541f, 6.51776f, 1.08344f)
                            curveTo(6.40724f, 1.22146f, 6.24642f, 1.30993f, 6.07067f, 1.32938f)
                            curveTo(4.71843f, 1.48428f, 3.47656f, 2.15025f, 2.59933f, 3.19094f)
                            curveTo(1.72211f, 4.23162f, 1.2759f, 5.56828f, 1.35207f, 6.92723f)
                            curveTo(1.42824f, 8.28618f, 2.02102f, 9.5646f, 3.00905f, 10.5007f)
                            curveTo(3.99707f, 11.4369f, 5.30559f, 11.9599f, 6.66667f, 11.9627f)
                            verticalLineTo(13.296f)
                            curveTo(2.98467f, 13.296f, 0f, 10.312f, 0f, 6.62938f)
                            curveTo(0f, 3.22538f, 2.56533f, 0.37738f, 5.924f, 0.00471f)
                            verticalLineTo(0.004f)
                            close()
                        }
                        path(
                            fill =
                                Brush.linearGradient(
                                    0f to Color.Black.copy(alpha = 0f),
                                    1f to Color.Black.copy(alpha = 0.55f),
                                    start = Offset(10f, 2.12637f),
                                    end = Offset(10f, 12.1217f),
                                ),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(9.548f, 1.32333f)
                            curveTo(9.64916f, 1.17838f, 9.80375f, 1.07953f, 9.97777f, 1.04853f)
                            curveTo(10.1518f, 1.01752f, 10.331f, 1.0569f, 10.476f, 1.158f)
                            curveTo(11.3585f, 1.77178f, 12.0794f, 2.59007f, 12.5769f, 3.54296f)
                            curveTo(13.0745f, 4.49585f, 13.334f, 5.55502f, 13.3333f, 6.63f)
                            curveTo(13.3333f, 10.312f, 10.3487f, 13.2967f, 6.66667f, 13.2967f)
                            verticalLineTo(11.9633f)
                            curveTo(7.80235f, 11.9635f, 8.90843f, 11.6011f, 9.82385f, 10.929f)
                            curveTo(10.7393f, 10.2569f, 11.4163f, 9.31007f, 11.7562f, 8.22647f)
                            curveTo(12.0962f, 7.14286f, 12.0813f, 5.97903f, 11.7139f, 4.90444f)
                            curveTo(11.3465f, 3.82984f, 10.6456f, 2.9006f, 9.71333f, 2.252f)
                            curveTo(9.64147f, 2.20191f, 9.58018f, 2.13814f, 9.53297f, 2.06435f)
                            curveTo(9.48576f, 1.99055f, 9.45356f, 1.90818f, 9.4382f, 1.82193f)
                            curveTo(9.42285f, 1.73569f, 9.42464f, 1.64726f, 9.44348f, 1.56171f)
                            curveTo(9.46232f, 1.47615f, 9.49784f, 1.39515f, 9.548f, 1.32333f)
                            close()
                        }
                    }.build()
            return _loading!!
        }

    val LogoType: ImageVector
        get() {
            if (_logoType != null) return _logoType!!
            _logoType =
                ImageVector
                    .Builder(
                        name = "LogoType",
                        defaultWidth = 105.dp,
                        defaultHeight = 37.dp,
                        viewportWidth = 105f,
                        viewportHeight = 37f,
                    ).apply {
                        path(
                            fill =
                                Brush.linearGradient(
                                    0f to Color(0xFF667EEA),
                                    1f to Color(0xFF764BA2),
                                    start =
                                        androidx.compose.ui.geometry
                                            .Offset(0f, 0f),
                                    end =
                                        androidx.compose.ui.geometry
                                            .Offset(105f, 37f),
                                ),
                            fillAlpha = 1.0f,
                            stroke = null,
                            strokeAlpha = 1.0f,
                            strokeLineWidth = 1.0f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Miter,
                            strokeLineMiter = 1.0f,
                            pathFillType = PathFillType.NonZero,
                        ) {
                            moveTo(0.4f, 36f)
                            verticalLineTo(21.5f)
                            curveTo(0.4f, 19.36f, 0.88f, 17.49f, 1.84f, 15.89f)
                            curveTo(2.83f, 14.26f, 4.21f, 12.99f, 5.97f, 12.1f)
                            curveTo(7.73f, 11.17f, 9.82f, 10.7f, 12.26f, 10.7f)
                            curveTo(13.47f, 10.7f, 14.59f, 10.83f, 15.62f, 11.09f)
                            curveTo(16.67f, 11.31f, 17.62f, 11.65f, 18.45f, 12.1f)
                            curveTo(19.31f, 12.54f, 20.05f, 13.06f, 20.66f, 13.63f)
                            horizontalLineTo(20.75f)
                            curveTo(21.39f, 13.06f, 22.13f, 12.54f, 22.96f, 12.1f)
                            curveTo(23.79f, 11.65f, 24.72f, 11.31f, 25.74f, 11.09f)
                            curveTo(26.8f, 10.83f, 27.94f, 10.7f, 29.15f, 10.7f)
                            curveTo(31.62f, 10.7f, 33.73f, 11.17f, 35.49f, 12.1f)
                            curveTo(37.28f, 12.99f, 38.66f, 14.26f, 39.62f, 15.89f)
                            curveTo(40.58f, 17.49f, 41.06f, 19.36f, 41.06f, 21.5f)
                            verticalLineTo(36f)
                            horizontalLineTo(32.7f)
                            verticalLineTo(21.89f)
                            curveTo(32.7f, 21.18f, 32.54f, 20.54f, 32.22f, 19.97f)
                            curveTo(31.9f, 19.39f, 31.46f, 18.93f, 30.88f, 18.58f)
                            curveTo(30.3f, 18.22f, 29.62f, 18.05f, 28.82f, 18.05f)
                            curveTo(28.05f, 18.05f, 27.36f, 18.22f, 26.75f, 18.58f)
                            curveTo(26.18f, 18.93f, 25.71f, 19.39f, 25.36f, 19.97f)
                            curveTo(25.04f, 20.54f, 24.88f, 21.18f, 24.88f, 21.89f)
                            verticalLineTo(36f)
                            horizontalLineTo(16.58f)
                            verticalLineTo(21.89f)
                            curveTo(16.58f, 21.18f, 16.4f, 20.54f, 16.05f, 19.97f)
                            curveTo(15.73f, 19.39f, 15.26f, 18.93f, 14.66f, 18.58f)
                            curveTo(14.08f, 18.22f, 13.39f, 18.05f, 12.59f, 18.05f)
                            curveTo(11.86f, 18.05f, 11.18f, 18.22f, 10.58f, 18.58f)
                            curveTo(10f, 18.93f, 9.54f, 19.39f, 9.18f, 19.97f)
                            curveTo(8.86f, 20.54f, 8.7f, 21.18f, 8.7f, 21.89f)
                            verticalLineTo(36f)
                            horizontalLineTo(0.4f)
                            close()
                            moveTo(58.49f, 36.58f)
                            curveTo(56f, 36.58f, 53.72f, 36.03f, 51.68f, 34.94f)
                            curveTo(49.66f, 33.82f, 48.04f, 32.29f, 46.83f, 30.34f)
                            curveTo(45.61f, 28.38f, 45f, 26.14f, 45f, 23.62f)
                            curveTo(45f, 21.12f, 45.61f, 18.9f, 46.83f, 16.94f)
                            curveTo(48.04f, 14.99f, 49.68f, 13.47f, 51.72f, 12.38f)
                            curveTo(53.77f, 11.26f, 56.04f, 10.7f, 58.54f, 10.7f)
                            curveTo(61.04f, 10.7f, 63.29f, 11.26f, 65.31f, 12.38f)
                            curveTo(67.36f, 13.47f, 68.97f, 14.99f, 70.16f, 16.94f)
                            curveTo(71.37f, 18.9f, 71.98f, 21.12f, 71.98f, 23.62f)
                            curveTo(71.98f, 26.14f, 71.37f, 28.38f, 70.16f, 30.34f)
                            curveTo(68.97f, 32.29f, 67.36f, 33.82f, 65.31f, 34.94f)
                            curveTo(63.29f, 36.03f, 61.02f, 36.58f, 58.49f, 36.58f)
                            close()
                            moveTo(58.49f, 29.18f)
                            curveTo(59.52f, 29.18f, 60.41f, 28.94f, 61.18f, 28.46f)
                            curveTo(61.98f, 27.98f, 62.59f, 27.33f, 63f, 26.5f)
                            curveTo(63.42f, 25.63f, 63.63f, 24.67f, 63.63f, 23.62f)
                            curveTo(63.63f, 22.53f, 63.42f, 21.57f, 63f, 20.74f)
                            curveTo(62.59f, 19.9f, 61.98f, 19.25f, 61.18f, 18.77f)
                            curveTo(60.41f, 18.29f, 59.52f, 18.05f, 58.49f, 18.05f)
                            curveTo(57.47f, 18.05f, 56.57f, 18.29f, 55.8f, 18.77f)
                            curveTo(55.04f, 19.25f, 54.43f, 19.9f, 53.98f, 20.74f)
                            curveTo(53.56f, 21.57f, 53.36f, 22.53f, 53.36f, 23.62f)
                            curveTo(53.36f, 24.67f, 53.56f, 25.63f, 53.98f, 26.5f)
                            curveTo(54.43f, 27.33f, 55.04f, 27.98f, 55.8f, 28.46f)
                            curveTo(56.57f, 28.94f, 57.47f, 29.18f, 58.49f, 29.18f)
                            close()
                            moveTo(75.92f, 36f)
                            verticalLineTo(0.96f)
                            horizontalLineTo(84.22f)
                            verticalLineTo(36f)
                            horizontalLineTo(75.92f)
                            close()
                            moveTo(91.92f, 24.82f)
                            verticalLineTo(18.53f)
                            curveTo(92.98f, 18.53f, 93.89f, 18.3f, 94.66f, 17.86f)
                            curveTo(95.46f, 17.41f, 96.05f, 16.82f, 96.43f, 16.08f)
                            curveTo(96.85f, 15.34f, 97.06f, 14.54f, 97.06f, 13.68f)
                            curveTo(97.06f, 12.75f, 96.85f, 11.94f, 96.43f, 11.23f)
                            curveTo(96.05f, 10.53f, 95.47f, 9.97f, 94.7f, 9.55f)
                            curveTo(93.94f, 9.14f, 93.02f, 8.93f, 91.97f, 8.93f)
                            horizontalLineTo(89.33f)
                            verticalLineTo(2.4f)
                            horizontalLineTo(92.59f)
                            curveTo(94.96f, 2.4f, 97.07f, 2.88f, 98.93f, 3.84f)
                            curveTo(100.78f, 4.77f, 102.24f, 6.06f, 103.3f, 7.73f)
                            curveTo(104.38f, 9.39f, 104.93f, 11.31f, 104.93f, 13.49f)
                            curveTo(104.93f, 15.66f, 104.38f, 17.62f, 103.3f, 19.34f)
                            curveTo(102.24f, 21.04f, 100.74f, 22.38f, 98.78f, 23.38f)
                            curveTo(96.86f, 24.34f, 94.58f, 24.82f, 91.92f, 24.82f)
                            close()
                            moveTo(93.6f, 36.62f)
                            curveTo(92.26f, 36.62f, 91.12f, 36.14f, 90.19f, 35.18f)
                            curveTo(89.26f, 34.22f, 88.8f, 33.09f, 88.8f, 31.78f)
                            curveTo(88.8f, 30.46f, 89.26f, 29.34f, 90.19f, 28.42f)
                            curveTo(91.12f, 27.46f, 92.26f, 26.98f, 93.6f, 26.98f)
                            curveTo(94.91f, 26.98f, 96.03f, 27.46f, 96.96f, 28.42f)
                            curveTo(97.92f, 29.34f, 98.4f, 30.46f, 98.4f, 31.78f)
                            curveTo(98.4f, 32.64f, 98.18f, 33.44f, 97.73f, 34.18f)
                            curveTo(97.31f, 34.91f, 96.74f, 35.5f, 96f, 35.95f)
                            curveTo(95.3f, 36.4f, 94.5f, 36.62f, 93.6f, 36.62f)
                            close()
                        }
                    }.build()
            return _logoType!!
        }

    private var _ad: ImageVector? = null
    private var _back: ImageVector? = null
    private var _check: ImageVector? = null
    private var _circle: ImageVector? = null
    private var _play: ImageVector? = null
    private var _fire: ImageVector? = null
    private var _close: ImageVector? = null
    private var _copy: ImageVector? = null
    private var _dropdownArrow: ImageVector? = null
    private var _pass: ImageVector? = null
    private var _timer: ImageVector? = null
    private var _fireFreeze: ImageVector? = null
    private var _adZero: ImageVector? = null
    private var _rocket: ImageVector? = null
    private var _warning: ImageVector? = null
    private var _more: ImageVector? = null
    private var _speaker: ImageVector? = null
    private var _loading: ImageVector? = null
    private var _logoType: ImageVector? = null
}
