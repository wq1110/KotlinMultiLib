package com.jw.media.lib.utils.ass

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import java.util.NavigableMap
import java.util.TreeMap

/**
 * Keys [Bitmaps][Bitmap] using both [ ][Bitmap.getAllocationByteCount] and the [Config]
 * returned from [Bitmap.getConfig].
 *
 *
 * Using both the config and the byte size allows us to safely re-use a greater variety of [ ], which increases the hit rate of the pool and therefore the
 * performance of applications. This class works around #301 by only allowing re-use of [ ] with a matching number of bytes per pixel.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class SizeConfigStrategy() : LruPoolStrategy {
    private val keyPool = KeyPool()
    private val groupedMap: GroupedLinkedMap<Key?, Bitmap> = GroupedLinkedMap()
    private val sortedSizes: MutableMap<Config?, NavigableMap<Int, Int>> = HashMap()

    override fun put(bitmap: Bitmap) {
        val size = Util.getBitmapByteSize(bitmap)
        val key = keyPool.get(size, bitmap.config)

        groupedMap.put(key, bitmap)

        val sizes = getSizesForConfig(bitmap.config)
        val current = sizes.get(key!!.size)
        sizes[key.size] = if (current == null) 1 else current + 1
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun get(width: Int, height: Int, config: Config?): Bitmap? {
        val size = Util.getBitmapByteSize(width, height, config)
        val bestKey = findBestKey(size, config)

        val result = groupedMap.get(bestKey)
        if (result != null) {
            // Decrement must be called before reconfigure.
            decrementBitmapOfSize(bestKey!!.size, result)
            result.reconfigure(width, height, (config)!!)
        }
        return result
    }

    private fun findBestKey(size: Int, config: Config?): Key? {
        var result = keyPool.get(size, config)
        for (possibleConfig: Config? in getInConfigs(config)) {
            val sizesForPossibleConfig = getSizesForConfig(possibleConfig)
            val possibleSize = sizesForPossibleConfig.ceilingKey(size)
            if (possibleSize != null && possibleSize <= size * MAX_SIZE_MULTIPLE) {
                if ((possibleSize != size
                            || (if (possibleConfig == null) config != null else !(possibleConfig == config)))
                ) {
                    keyPool.offer(result)
                    result = keyPool.get(possibleSize, possibleConfig)
                }
                break
            }
        }
        return result
    }

    override fun removeLast(): Bitmap? {
        val removed = groupedMap.removeLast()
        if (removed != null) {
            val removedSize = Util.getBitmapByteSize(removed)
            decrementBitmapOfSize(removedSize, removed)
        }
        return removed
    }

    private fun decrementBitmapOfSize(size: Int, removed: Bitmap) {
        val config = removed.config
        val sizes = getSizesForConfig(config)
        val current = sizes.get(size)
            ?: throw NullPointerException(
                (("Tried to decrement empty size"
                        + ", size: "
                        + size
                        + ", removed: "
                        + logBitmap(removed)
                        + ", this: "
                        + this))
            )

        if (current == 1) {
            sizes.remove(size)
        } else {
            sizes[size] = current - 1
        }
    }

    private fun getSizesForConfig(config: Config?): NavigableMap<Int, Int> {
        var sizes = sortedSizes.get(config)
        if (sizes == null) {
            sizes = TreeMap()
            sortedSizes[config] = sizes
        }
        return sizes
    }

    override fun logBitmap(bitmap: Bitmap): String {
        val size = Util.getBitmapByteSize(bitmap)
        return getBitmapString(size, bitmap.config)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun logBitmap(width: Int, height: Int, config: Config?): String {
        val size = Util.getBitmapByteSize(width, height, config)
        return getBitmapString(size, config)
    }

    override fun getSize(bitmap: Bitmap?): Int {
        return Util.getBitmapByteSize((bitmap)!!)
    }

    override fun toString(): String {
        val sb =
            StringBuilder()
                .append("SizeConfigStrategy{groupedMap=")
                .append(groupedMap)
                .append(", sortedSizes=(")
        for (entry: Map.Entry<Config?, NavigableMap<Int, Int>> in sortedSizes.entries) {
            sb.append(entry.key).append('[').append(entry.value).append("], ")
        }
        if (!sortedSizes.isEmpty()) {
            sb.replace(sb.length - 2, sb.length, "")
        }
        return sb.append(")}").toString()
    }

    @VisibleForTesting
    internal class KeyPool() : BaseKeyPool<Key?>() {
        fun get(size: Int, config: Config?): Key? {
            val result = get()
            result!!.init(size, config)
            return result
        }

        override fun create(): Key {
            return Key(this)
        }
    }

    @VisibleForTesting
    internal class Key(private val pool: KeyPool) : Poolable {
        var size: Int = 0
        private var config: Config? = null

        @VisibleForTesting
        constructor(pool: KeyPool, size: Int, config: Config?) : this(pool) {
            init(size, config)
        }

        fun init(size: Int, config: Config?) {
            this.size = size
            this.config = config
        }

        override fun offer() {
            pool.offer(this)
        }

        override fun toString(): String {
            return getBitmapString(size, config)
        }

        override fun equals(o: Any?): Boolean {
            if (o is Key) {
                val other = o
                return size == other.size && Util.bothNullOrEqual(config, other.config)
            }
            return false
        }

        override fun hashCode(): Int {
            var result = size
            result = 31 * result + (if (config != null) config.hashCode() else 0)
            return result
        }
    }

    companion object {
        private val MAX_SIZE_MULTIPLE = 4

        private val ARGB_8888_IN_CONFIGS: Array<Config?>

        init {
            var result =
                arrayOf(
                    Config.ARGB_8888,  // The value returned by Bitmaps with the hidden Bitmap config.
                    null,
                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = result.copyOf(result.size + 1)
                result.get(result.size - 1) = Config.RGBA_F16
            }
            ARGB_8888_IN_CONFIGS = result
        }

        private val RGBA_F16_IN_CONFIGS = ARGB_8888_IN_CONFIGS

        // We probably could allow ARGB_4444 and RGB_565 to decode into each other, but ARGB_4444 is
        // deprecated and we'd rather be safe.
        private val RGB_565_IN_CONFIGS = arrayOf<Config?>(Config.RGB_565)
        private val ARGB_4444_IN_CONFIGS = arrayOf<Config?>(Config.ARGB_4444)
        private val ALPHA_8_IN_CONFIGS = arrayOf<Config?>(Config.ALPHA_8)

        fun getBitmapString(size: Int, config: Config?): String {
            return "[$size]($config)"
        }

        private fun getInConfigs(requested: Config?): Array<Config?> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if ((Config.RGBA_F16 == requested)) { // NOPMD - Avoid short circuiting sdk checks.
                    return RGBA_F16_IN_CONFIGS
                }
            }

            when (requested) {
                Config.ARGB_8888 -> return ARGB_8888_IN_CONFIGS
                Config.RGB_565 -> return RGB_565_IN_CONFIGS
                Config.ARGB_4444 -> return ARGB_4444_IN_CONFIGS
                Config.ALPHA_8 -> return ALPHA_8_IN_CONFIGS
                else -> return arrayOf(requested)
            }
        }
    }
}