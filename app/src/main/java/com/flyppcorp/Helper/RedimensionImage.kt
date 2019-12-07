package com.flyppcorp.Helper

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.grpc.internal.SharedResourceHolder
import io.opencensus.resource.Resource

class RedimensionImage {

    fun calcularTamanho (options : BitmapFactory.Options, larguraDesejada : Int, alturaDesejada: Int) : Int{

        val altura = options.outHeight
        val largura = options.outWidth
        var fator = 1

        if (altura > alturaDesejada || largura > larguraDesejada){
            val metadeAltura = altura/2
            val metadeLargura = largura/2
            while ((metadeAltura/fator) > alturaDesejada && (metadeLargura/fator) > larguraDesejada){
                fator *= 2
            }
        }
        return fator

    }

    fun redimensionarResource (res : Resources, resId : Int, larguraDesejada: Int, alturaDesejada: Int) : Bitmap{

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)
        options.inSampleSize = calcularTamanho(options, larguraDesejada, alturaDesejada)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)

    }
}