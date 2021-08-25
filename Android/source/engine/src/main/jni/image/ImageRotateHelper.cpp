//
// Created by Hebing Shi on 2021/7/27.
//

#include <arm_neon.h>
#include <log.h>
#include "ImageRotateHelper.h"

static const uint8_t kVTbl4x4Transpose[] = {0, 4, 8, 12, 1, 5, 9, 13,
                                            2, 6, 10, 14, 3, 7, 11, 15};
#if defined(__aarch64__) && defined(__ARM_NEON)

void transW8C2(const uint8_t *src,
               int src_stride,
               uint8_t *dst,
               int dst_stride,
               int width) {
    const uint8_t *src_temp = src;
    asm volatile(
    "sub         %w3, %w3, #8                  \n"
    "1:                                        \n"
    "mov         %0, %1                        \n"
    "ld1         {v0.16b}, [%0], %4            \n"
    "ld1         {v1.16b}, [%0], %4            \n"
    "ld1         {v2.16b}, [%0], %4            \n"
    "ld1         {v3.16b}, [%0], %4            \n"
    "ld1         {v4.16b}, [%0], %4            \n"
    "ld1         {v5.16b}, [%0], %4            \n"
    "ld1         {v6.16b}, [%0], %4            \n"
    "ld1         {v7.16b}, [%0]                \n"

    "trn1        v16.16b, v0.16b, v1.16b       \n"
    "trn2        v17.16b, v0.16b, v1.16b       \n"
    "trn1        v18.16b, v2.16b, v3.16b       \n"
    "trn2        v19.16b, v2.16b, v3.16b       \n"
    "trn1        v20.16b, v4.16b, v5.16b       \n"
    "trn2        v21.16b, v4.16b, v5.16b       \n"
    "trn1        v22.16b, v6.16b, v7.16b       \n"
    "trn2        v23.16b, v6.16b, v7.16b       \n"

    "trn1        v0.8h, v16.8h, v18.8h         \n"
    "trn2        v1.8h, v16.8h, v18.8h         \n"
    "trn1        v2.8h, v20.8h, v22.8h         \n"
    "trn2        v3.8h, v20.8h, v22.8h         \n"
    "trn1        v4.8h, v17.8h, v19.8h         \n"
    "trn2        v5.8h, v17.8h, v19.8h         \n"
    "trn1        v6.8h, v21.8h, v23.8h         \n"
    "trn2        v7.8h, v21.8h, v23.8h         \n"

    "trn1        v16.4s, v0.4s, v2.4s          \n"
    "trn2        v17.4s, v0.4s, v2.4s          \n"
    "trn1        v18.4s, v1.4s, v3.4s          \n"
    "trn2        v19.4s, v1.4s, v3.4s          \n"
    "trn1        v20.4s, v4.4s, v6.4s          \n"
    "trn2        v21.4s, v4.4s, v6.4s          \n"
    "trn1        v22.4s, v5.4s, v7.4s          \n"
    "trn2        v23.4s, v5.4s, v7.4s          \n"
    "mov         %0, %2                        \n"

    "zip1        v12.16b, v16.16b, v20.16b \n"
    "zip1        v13.16b, v18.16b, v22.16b \n"
    "zip1        v14.16b, v17.16b, v21.16b \n"
    "zip1        v15.16b, v19.16b, v23.16b \n"
    "zip2        v8.16b, v16.16b, v20.16b \n"
    "zip2        v9.16b, v18.16b, v22.16b \n"
    "zip2        v10.16b, v17.16b, v21.16b \n"
    "zip2        v11.16b, v19.16b, v23.16b \n"

    "st1    {v12.16b}, [%0], %5\n"
    "st1    {v13.16b}, [%0], %5\n"
    "st1    {v14.16b}, [%0], %5\n"
    "st1    {v15.16b}, [%0], %5\n"
    "st1    {v8.16b},  [%0], %5\n"
    "st1    {v9.16b},  [%0], %5\n"
    "st1    {v10.16b}, [%0], %5\n"
    "st1    {v11.16b}, [%0]    \n"

    "add         %1, %1, #16                   \n"  // src += 8*2
    "add         %2, %2, %5, lsl #3            \n"  // dst += 8 * dst_stride
    "subs        %w3, %w3,  #8                 \n"  // w     -= 8
    "b.ge        1b                            \n"
    "adds        %w3, %w3, #8                  \n"
    "b.eq        4f                            \n"
    "4:                                        \n"
    :"=&r"(src_temp),                        // %0
    "+r"(src),                            // %1
    "+r"(dst),                                  // %2
    "+r"(width)                               // %3
    : "r"(static_cast<ptrdiff_t>(src_stride)),                        // %4
    "r"(static_cast<ptrdiff_t>(dst_stride))                           //%5
    : "memory", "cc", "v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7",
    "v8", "v9", "v10", "v11", "v12", "v13", "v16",
    "v17", "v18", "v19", "v20", "v21", "v22", "v23", "v30", "v31");
}

void transW8C1(const uint8_t *src,
               int src_stride,
               uint8_t *dst,
               int dst_stride,
               int width) {
    const uint8_t *src_temp;
    asm volatile(
    // loops are on blocks of 8. loop will stop when
    // counter gets to or below 0. starting the counter
    // at w-8 allow for this
    "sub         %w3, %w3, #8                  \n"
    // handle 8x8 blocks. this should be the majority of the plane
    "1:                                        \n"
    "mov         %0, %1                        \n"
    "ld1         {v0.8b}, [%0], %5             \n"
    "ld1         {v1.8b}, [%0], %5             \n"
    "ld1         {v2.8b}, [%0], %5             \n"
    "ld1         {v3.8b}, [%0], %5             \n"
    "ld1         {v4.8b}, [%0], %5             \n"
    "ld1         {v5.8b}, [%0], %5             \n"
    "ld1         {v6.8b}, [%0], %5             \n"
    "ld1         {v7.8b}, [%0]                 \n"
    "mov         %0, %1                        \n"
    "trn2        v16.8b, v0.8b, v1.8b          \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // prefetch 7 lines ahead
    "trn1        v17.8b, v0.8b, v1.8b          \n"
    "add         %0, %0, %5                    \n"
    "trn2        v18.8b, v2.8b, v3.8b          \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // row 1
    "trn1        v19.8b, v2.8b, v3.8b          \n"
    "add         %0, %0, %5                    \n"
    "trn2        v20.8b, v4.8b, v5.8b          \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // row 2
    "trn1        v21.8b, v4.8b, v5.8b          \n"
    "add         %0, %0, %5                    \n"
    "trn2        v22.8b, v6.8b, v7.8b          \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // row 3
    "trn1        v23.8b, v6.8b, v7.8b          \n"
    "add         %0, %0, %5                    \n"
    "trn2        v3.4h, v17.4h, v19.4h         \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // row 4
    "trn1        v1.4h, v17.4h, v19.4h         \n"
    "add         %0, %0, %5                    \n"
    "trn2        v2.4h, v16.4h, v18.4h         \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // row 5
    "trn1        v0.4h, v16.4h, v18.4h         \n"
    "add         %0, %0, %5                    \n"
    "trn2        v7.4h, v21.4h, v23.4h         \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // row 6
    "trn1        v5.4h, v21.4h, v23.4h         \n"
    "add         %0, %0, %5                    \n"
    "trn2        v6.4h, v20.4h, v22.4h         \n"
    //"prfm        pldl1keep, [%0, 448]          \n"  // row 7
    "trn1        v4.4h, v20.4h, v22.4h         \n"
    "trn2        v21.2s, v1.2s, v5.2s          \n"
    "trn1        v17.2s, v1.2s, v5.2s          \n"
    "trn2        v20.2s, v0.2s, v4.2s          \n"
    "trn1        v16.2s, v0.2s, v4.2s          \n"
    "trn2        v23.2s, v3.2s, v7.2s          \n"
    "trn1        v19.2s, v3.2s, v7.2s          \n"
    "trn2        v22.2s, v2.2s, v6.2s          \n"
    "trn1        v18.2s, v2.2s, v6.2s          \n"
    "mov         %0, %2                        \n"
    "st1         {v17.8b}, [%0], %6            \n"
    "st1         {v16.8b}, [%0], %6            \n"
    "st1         {v19.8b}, [%0], %6            \n"
    "st1         {v18.8b}, [%0], %6            \n"
    "st1         {v21.8b}, [%0], %6            \n"
    "st1         {v20.8b}, [%0], %6            \n"
    "st1         {v23.8b}, [%0], %6            \n"
    "st1         {v22.8b}, [%0]                \n"
    "add         %1, %1, #8                    \n"  // src += 8
    "add         %2, %2, %6, lsl #3            \n"  // dst += 8 * dst_stride
    "subs        %w3, %w3, #8                  \n"  // w   -= 8
    "b.ge        1b                            \n"
    // add 8 back to counter. if the result is 0 there are
    // no residuals.
    "adds        %w3, %w3, #8                  \n"
    "b.eq        4f                            \n"
    // some residual, so between 1 and 7 lines left to transpose
    "cmp         %w3, #2                       \n"
    "b.lt        3f                            \n"
    "cmp         %w3, #4                       \n"
    "b.lt        2f                            \n"
    // 4x8 block
    "mov         %0, %1                        \n"
    "ld1         {v0.s}[0], [%0], %5           \n"
    "ld1         {v0.s}[1], [%0], %5           \n"
    "ld1         {v0.s}[2], [%0], %5           \n"
    "ld1         {v0.s}[3], [%0], %5           \n"
    "ld1         {v1.s}[0], [%0], %5           \n"
    "ld1         {v1.s}[1], [%0], %5           \n"
    "ld1         {v1.s}[2], [%0], %5           \n"
    "ld1         {v1.s}[3], [%0]               \n"
    "mov         %0, %2                        \n"
    "ld1         {v2.16b}, [%4]                \n"
    "tbl         v3.16b, {v0.16b}, v2.16b      \n"
    "tbl         v0.16b, {v1.16b}, v2.16b      \n"
    // TODO(frkoenig): Rework shuffle above to
    // write out with 4 instead of 8 writes.
    "st1 {v3.s}[0], [%0], %6                     \n"
    "st1 {v3.s}[1], [%0], %6                     \n"
    "st1 {v3.s}[2], [%0], %6                     \n"
    "st1 {v3.s}[3], [%0]                         \n"
    "add         %0, %2, #4                      \n"
    "st1 {v0.s}[0], [%0], %6                     \n"
    "st1 {v0.s}[1], [%0], %6                     \n"
    "st1 {v0.s}[2], [%0], %6                     \n"
    "st1 {v0.s}[3], [%0]                         \n"
    "add         %1, %1, #4                      \n"  // src += 4
    "add         %2, %2, %6, lsl #2              \n"  // dst += 4 * dst_stride
    "subs        %w3, %w3, #4                    \n"  // w   -= 4
    "b.eq        4f                              \n"
    // some residual, check to see if it includes a 2x8 block,
    // or less
    "cmp         %w3, #2                         \n"
    "b.lt        3f                              \n"
    // 2x8 block
    "2:                                          \n"
    "mov         %0, %1                          \n"
    "ld1     {v0.h}[0], [%0], %5                 \n"
    "ld1     {v1.h}[0], [%0], %5                 \n"
    "ld1     {v0.h}[1], [%0], %5                 \n"
    "ld1     {v1.h}[1], [%0], %5                 \n"
    "ld1     {v0.h}[2], [%0], %5                 \n"
    "ld1     {v1.h}[2], [%0], %5                 \n"
    "ld1     {v0.h}[3], [%0], %5                 \n"
    "ld1     {v1.h}[3], [%0]                     \n"
    "trn2    v2.8b, v0.8b, v1.8b                 \n"
    "trn1    v3.8b, v0.8b, v1.8b                 \n"
    "mov         %0, %2                          \n"
    "st1     {v3.8b}, [%0], %6                   \n"
    "st1     {v2.8b}, [%0]                       \n"
    "add         %1, %1, #2                      \n"  // src += 2
    "add         %2, %2, %6, lsl #1              \n"  // dst += 2 * dst_stride
    "subs        %w3, %w3,  #2                   \n"  // w   -= 2
    "b.eq        4f                              \n"
    // 1x8 block
    "3:                                          \n"
    "ld1         {v0.b}[0], [%1], %5             \n"
    "ld1         {v0.b}[1], [%1], %5             \n"
    "ld1         {v0.b}[2], [%1], %5             \n"
    "ld1         {v0.b}[3], [%1], %5             \n"
    "ld1         {v0.b}[4], [%1], %5             \n"
    "ld1         {v0.b}[5], [%1], %5             \n"
    "ld1         {v0.b}[6], [%1], %5             \n"
    "ld1         {v0.b}[7], [%1]                 \n"
    "st1         {v0.8b}, [%2]                   \n"
    "4:                                          \n"
    : "=&r"(src_temp),                          // %0
    "+r"(src),                                // %1
    "+r"(dst),                                // %2
    "+r"(width)                               // %3
    : "r"(&kVTbl4x4Transpose),                  // %4
    "r"(static_cast<ptrdiff_t>(src_stride)),  // %5
    "r"(static_cast<ptrdiff_t>(dst_stride))   // %6
    : "memory", "cc", "v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v16",
    "v17", "v18", "v19", "v20", "v21", "v22", "v23");
}

#endif


static void
rotate270(const unsigned char *src, int srcw, int srch, int srcstride, unsigned char *dst,
          int /*w*/, int h, int stride) {
    const int srcwgap = srcstride - srcw;

    // point to the last dst pixel row
    unsigned char *dstend = dst + stride * (h - 1);

    const unsigned char *src0 = src;

    int y = 0;
#if __ARM_NEON
    for (; y + 7 < srch; y += 8) {
#if __aarch64__
        transW8C1(src0, srcstride, dstend, -stride, srcw);
        src0 += 8 * srcstride;
        dstend += 8;
#else
        const unsigned char *src1 = src0 + srcstride;
        unsigned char *dst7 = dstend + y;
        unsigned char *dst6 = dstend + y - stride;
        int src_step = 2 * srcstride;
        int dst_step = -2 * stride;

        int nn = srcw >> 3;
        int remain = srcw - (nn << 3);
        if (nn > 0)
        {
            asm volatile(
                "0:                             \n"
                "pld        [%1, #64]           \n"
                "vld1.u8    {d0}, [%1], %10     \n"

                "pld        [%2, #64]           \n"
                "vld1.u8    {d1}, [%2], %10     \n"

                "pld        [%1, #64]           \n"
                "vld1.u8    {d2}, [%1], %10     \n"

                "vtrn.u8    d0, d1              \n" // _src01t_r

                "pld        [%2, #64]           \n"
                "vld1.u8    {d3}, [%2], %10     \n"

                "pld        [%1, #64]           \n"
                "vld1.u8    {d4}, [%1], %10     \n"

                "vtrn.u8    d2, d3              \n" // _src23t_r

                "pld        [%2, #64]           \n"
                "vld1.u8    {d5}, [%2], %10     \n"

                "pld        [%1, #64]           \n"
                "vld1.u8    {d6}, [%1], %10     \n"

                "vtrn.u8    d4, d5              \n" // _src45t_r

                "pld        [%2, #64]           \n"
                "vld1.u8    {d7}, [%2], %10     \n"

                "vtrn.u8    d6, d7              \n" // _src67t_r

                "sub        %1, %1, %10, lsl #2 \n" // restore src0

                "vtrn.u16   q0, q1              \n" // _src02tt_r _src13tt_r

                "sub        %2, %2, %10, lsl #2 \n" // restore src1

                "vtrn.u16   q2, q3              \n" // _src46tt_r _src57tt_r

                "add        %1, #8              \n" // src0 += 8

                "vtrn.u32   q0, q2              \n" // _src04ttt_r _src15ttt_r

                "add        %2, #8              \n" // src1 += 8

                "vtrn.u32   q1, q3              \n" // _src26ttt_r _src37ttt_r
                "vst1.u8    {d0}, [%3], %11     \n"
                "vst1.u8    {d1}, [%4], %11     \n"

                "subs       %0, #1              \n"

                "vst1.u8    {d2}, [%3], %11     \n"
                "vst1.u8    {d3}, [%4], %11     \n"
                "vst1.u8    {d4}, [%3], %11     \n"
                "vst1.u8    {d5}, [%4], %11     \n"
                "vst1.u8    {d6}, [%3], %11     \n"
                "vst1.u8    {d7}, [%4], %11     \n"

                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(src1), // %2
                "=r"(dst7), // %3
                "=r"(dst6)  // %4
                : "0"(nn),
                "1"(src0),
                "2"(src1),
                "3"(dst7),
                "4"(dst6),
                "r"(src_step), // %10
                "r"(dst_step)  // %11
                : "cc", "memory", "q0", "q1", "q2", "q3");
        }

        for (; remain > 0; remain--) {
            dst7[0] = src0[0];
            dst7[1] = src1[0];
            dst7[2] = src0[0 + src_step];
            dst7[3] = src1[0 + src_step];
            dst7[4] = src0[0 + 2 * src_step];
            dst7[5] = src1[0 + 2 * src_step];
            dst7[6] = src0[0 + 3 * src_step];
            dst7[7] = src1[0 + 3 * src_step];

            src0 += 1;
            src1 += 1;

            dst7 -= stride;
        }

        src0 += srcwgap + 7 * srcstride;
#endif
    }
#endif // __ARM_NEON
    for (; y < srch; y++) {
        unsigned char *dst0 = dstend + y;

        int x = 0;
        for (; x < srcw; x++) {
            *dst0 = *src0;

            src0 += 1;
            dst0 -= stride;
        }

        src0 += srcwgap;
    }
}

static void
rotate270c2(const unsigned char *src, int srcw, int srch, int srcstride, unsigned char *dst,
            int /*w*/, int h, int stride) {
    const int srcwgap = srcstride - srcw * 2;

    // point to the last dst pixel row
    unsigned char *dstend = dst + stride * (h - 1);

    const unsigned char *src0 = src;

    int y = 0;
#if __ARM_NEON
    for (; y + 7 < srch; y += 8) {
        const unsigned char *src1 = src0 + srcstride;

        unsigned char *dst7 = dstend + y * 2;
        unsigned char *dst6 = dstend + y * 2 - stride;

        int src_step = 2 * srcstride;
        int dst_step = -2 * stride;

        int nn = srcw >> 3;
        int remain = srcw - (nn << 3);

#if __aarch64__
        const unsigned char *srcP = src + y * srcstride;
        transW8C2(srcP, srcstride, dst7, -stride, srcw);
#else
        if (nn > 0)
        {
            asm volatile(
                "0:                             \n"
                "pld        [%1, #128]          \n"
                "vld2.u8    {d0-d1}, [%1], %10  \n"

                "pld        [%2, #128]          \n"
                "vld2.u8    {d2-d3}, [%2], %10  \n"

                "pld        [%1, #128]          \n"
                "vld2.u8    {d4-d5}, [%1], %10  \n"

                "vtrn.u8    q0, q1              \n" // _src01t_r

                "pld        [%2, #128]          \n"
                "vld2.u8    {d6-d7}, [%2], %10  \n"

                "pld        [%1, #128]          \n"
                "vld2.u8    {d16-d17}, [%1], %10\n"

                "vtrn.u8    q2, q3              \n" // _src23t_r

                "pld        [%2, #128]          \n"
                "vld2.u8    {d18-d19}, [%2], %10\n"

                "pld        [%1, #128]          \n"
                "vld2.u8    {d20-d21}, [%1], %10\n"

                "vtrn.u8    q8, q9              \n" // _src45t_r

                "pld        [%2, #128]          \n"
                "vld2.u8    {d22-d23}, [%2], %10\n"

                "vtrn.u8    q10, q11            \n" // _src67t_r

                "sub        %1, %1, %10, lsl #2 \n" // restore src0

                "vtrn.u16   q0, q2              \n" // _src02tt_r

                "sub        %2, %2, %10, lsl #2 \n" // restore src1

                "vtrn.u16   q1, q3              \n" // _src13tt_r

                "add        %1, #16             \n" // src0 += 16

                "vtrn.u16   q8, q10             \n" // _src46tt_r

                "add        %2, #16             \n" // src1 += 16

                "vtrn.u16   q9, q11             \n" // _src57tt_r

                "vtrn.u32   q0, q8              \n" // _src04ttt_r

                "vtrn.u32   q1, q9              \n" // _src15ttt_r
                "vst2.u8    {d0-d1}, [%3], %11  \n"

                "vtrn.u32   q2, q10             \n" // _src26ttt_r
                "vst2.u8    {d2-d3}, [%4], %11  \n"

                "vtrn.u32   q3, q11             \n" // _src37ttt_r
                "vst2.u8    {d4-d5}, [%3], %11  \n"

                "subs       %0, #1              \n"

                "vst2.u8    {d16-d17}, [%3], %11\n"
                "vst2.u8    {d6-d7}, [%4], %11  \n"
                "vst2.u8    {d18-d19}, [%4], %11\n"
                "vst2.u8    {d20-d21}, [%3], %11\n"
                "vst2.u8    {d22-d23}, [%4], %11\n"

                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(src1), // %2
                "=r"(dst7), // %3
                "=r"(dst6)  // %4
                : "0"(nn),
                "1"(src0),
                "2"(src1),
                "3"(dst7),
                "4"(dst6),
                "r"(src_step), // %10
                "r"(dst_step)  // %11
                : "cc", "memory", "q0", "q1", "q2", "q3", "q8", "q9", "q10", "q11");
        }
#endif // __aarch64__
        for (; remain > 0; remain--) {
            dst7[0] = src0[0];
            dst7[1] = src0[1];
            dst7[2] = src1[0];
            dst7[3] = src1[1];
            dst7[4] = src0[0 + src_step];
            dst7[5] = src0[1 + src_step];
            dst7[6] = src1[0 + src_step];
            dst7[7] = src1[1 + src_step];
            dst7[8] = src0[0 + 2 * src_step];
            dst7[9] = src0[1 + 2 * src_step];
            dst7[10] = src1[0 + 2 * src_step];
            dst7[11] = src1[1 + 2 * src_step];
            dst7[12] = src0[0 + 3 * src_step];
            dst7[13] = src0[1 + 3 * src_step];
            dst7[14] = src1[0 + 3 * src_step];
            dst7[15] = src1[1 + 3 * src_step];

            src0 += 2;
            src1 += 2;

            dst7 -= stride;
        }

        src0 += srcwgap + 7 * srcstride;
    }
#endif // __ARM_NEON
    for (; y < srch; y++) {
        unsigned char *dst0 = dstend + y * 2;

        int x = 0;
        for (; x < srcw; x++) {
            dst0[0] = src0[0];
            dst0[1] = src0[1];

            src0 += 2;
            dst0 -= stride;
        }

        src0 += srcwgap;
    }
}

void
rotate90C1(const unsigned char *src, int srcw, int srch, int srcstride, unsigned char *dst, int w,
           int /*h*/, int stride) {
    const int srcwgap = srcstride - srcw;

    // point to the last dst pixel in row
    unsigned char *dstend = dst + w;

    const unsigned char *src0 = src;

    int y = 0;
#if __ARM_NEON
    for (; y + 7 < srch; y += 8) {
        const unsigned char *src1 = src0 + srcstride;

        unsigned char *dst0 = dstend - y - 8;
        unsigned char *dst1 = dstend - y - 8 + stride;

        int src_step = 2 * srcstride;
        int dst_step = 2 * stride;

        int nn = srcw >> 3;
        int remain = srcw - (nn << 3);
#if __aarch64__
        auto srcTrans = src + 7 * srcstride + y * srcstride;
        auto dstTrans = dstend - y - 8;
        transW8C1(srcTrans, -srcstride, dstTrans, stride, srcstride);
#else
        if (nn > 0)
        {
            asm volatile(
                "0:                             \n"
                "pld        [%1, #64]           \n"
                "vld1.u8    {d0}, [%1], %10     \n"

                "pld        [%2, #64]           \n"
                "vld1.u8    {d1}, [%2], %10     \n"

                "pld        [%1, #64]           \n"
                "vld1.u8    {d2}, [%1], %10     \n"

                "vtrn.u8    d1, d0              \n" // _src01t_r

                "pld        [%2, #64]           \n"
                "vld1.u8    {d3}, [%2], %10     \n"

                "pld        [%1, #64]           \n"
                "vld1.u8    {d4}, [%1], %10     \n"

                "vtrn.u8    d3, d2              \n" // _src23t_r

                "pld        [%2, #64]           \n"
                "vld1.u8    {d5}, [%2], %10     \n"

                "pld        [%1, #64]           \n"
                "vld1.u8    {d6}, [%1], %10     \n"

                "vtrn.u8    d5, d4              \n" // _src45t_r

                "pld        [%2, #64]           \n"
                "vld1.u8    {d7}, [%2], %10     \n"

                "vtrn.u8    d7, d6              \n" // _src67t_r

                "sub        %1, %1, %10, lsl #2 \n" // restore src0

                "vtrn.u16   q1, q0              \n" // _src02tt_r _src13tt_r

                "sub        %2, %2, %10, lsl #2 \n" // restore src1

                "vtrn.u16   q3, q2              \n" // _src46tt_r _src57tt_r

                "add        %1, #8              \n" // src0 += 8

                "vtrn.u32   q3, q1              \n" // _src26ttt_r _src37ttt_r

                "add        %2, #8              \n" // src1 += 8

                "vtrn.u32   q2, q0              \n" // _src04ttt_r _src15ttt_r
                "vst1.u8    {d6}, [%4], %11     \n"
                "vst1.u8    {d7}, [%3], %11     \n"

                "subs       %0, #1              \n"

                "vst1.u8    {d4}, [%4], %11     \n"
                "vst1.u8    {d5}, [%3], %11     \n"
                "vst1.u8    {d2}, [%4], %11     \n"
                "vst1.u8    {d3}, [%3], %11     \n"
                "vst1.u8    {d0}, [%4], %11     \n"
                "vst1.u8    {d1}, [%3], %11     \n"

                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(src1), // %2
                "=r"(dst0), // %3
                "=r"(dst1)  // %4
                : "0"(nn),
                "1"(src0),
                "2"(src1),
                "3"(dst0),
                "4"(dst1),
                "r"(src_step), // %10
                "r"(dst_step)  // %11
                : "cc", "memory", "q0", "q1", "q2", "q3");
        }
        for (; remain > 0; remain--)
        {
            dst0[0] = src1[0 + 3 * src_step];
            dst0[1] = src0[0 + 3 * src_step];
            dst0[2] = src1[0 + 2 * src_step];
            dst0[3] = src0[0 + 2 * src_step];
            dst0[4] = src1[0 + src_step];
            dst0[5] = src0[0 + src_step];
            dst0[6] = src1[0];
            dst0[7] = src0[0];

            src0 += 1;
            src1 += 1;

            dst0 += stride;
        }
#endif

        src0 += srcwgap + 7 * srcstride;
    }
#endif // __ARM_NEON
    for (; y < srch; y++) {
        unsigned char *dst0 = dstend - y - 1;

        int x = 0;
        for (; x < srcw; x++) {
            *dst0 = *src0;

            src0 += 1;
            dst0 += stride;
        }

        src0 += srcwgap;
    }
}

static void
rotate90C2(const unsigned char *src, int srcw, int srch, int srcstride, unsigned char *dst,
           int w, int /*h*/, int stride) {
    const int srcwgap = srcstride - srcw * 2;

    // point to the last dst pixel in row
    unsigned char *dstend = dst + w * 2;

    const unsigned char *src0 = src;

    int y = 0;
#if __ARM_NEON
    for (; y + 7 < srch; y += 8) {
        const unsigned char *src1 = src0 + srcstride;

        unsigned char *dst0 = dstend - y * 2 - 8 * 2;
        unsigned char *dst1 = dstend - y * 2 - 8 * 2 + stride;

        int src_step = 2 * srcstride;
        int dst_step = 2 * stride;

        int nn = srcw >> 3;
        int remain = srcw - (nn << 3);

#if __aarch64__
        auto srcTran = src + y * srcstride + 7 * srcstride;
        auto dstTran = dstend - y * 2 - 8 * 2;
        transW8C2(srcTran, -srcstride, dstTran, stride, srcw);
#else
        if (nn > 0)
        {
            asm volatile(
                "0:                             \n"
                "pld        [%1, #128]          \n"
                "vld2.u8    {d0-d1}, [%1], %10  \n"

                "pld        [%2, #128]          \n"
                "vld2.u8    {d2-d3}, [%2], %10  \n"

                "pld        [%1, #128]          \n"
                "vld2.u8    {d4-d5}, [%1], %10  \n"

                "vtrn.u8    q1, q0              \n" // _src01t_r

                "pld        [%2, #128]          \n"
                "vld2.u8    {d6-d7}, [%2], %10  \n"

                "pld        [%1, #128]          \n"
                "vld2.u8    {d16-d17}, [%1], %10\n"

                "vtrn.u8    q3, q2              \n" // _src23t_r

                "pld        [%2, #128]          \n"
                "vld2.u8    {d18-d19}, [%2], %10\n"

                "pld        [%1, #128]          \n"
                "vld2.u8    {d20-d21}, [%1], %10\n"

                "vtrn.u8    q9, q8              \n" // _src45t_r

                "pld        [%2, #128]          \n"
                "vld2.u8    {d22-d23}, [%2], %10\n"

                "vtrn.u8    q11, q10            \n" // _src67t_r

                "sub        %1, %1, %10, lsl #2 \n" // restore src0

                "vtrn.u16   q2, q0              \n" // _src02tt_r

                "sub        %2, %2, %10, lsl #2 \n" // restore src1

                "vtrn.u16   q3, q1              \n" // _src13tt_r

                "add        %1, #16             \n" // src0 += 16

                "vtrn.u16   q10, q8             \n" // _src46tt_r

                "add        %2, #16             \n" // src1 += 16

                "vtrn.u16   q11, q9             \n" // _src57tt_r

                "vtrn.u32   q10, q2             \n" // _src26ttt_r

                "vtrn.u32   q11, q3             \n" // _src37ttt_r
                "vst2.u8    {d20-d21}, [%4], %11\n"

                "vtrn.u32   q8, q0              \n" // _src04ttt_r
                "vst2.u8    {d22-d23}, [%3], %11\n"

                "vtrn.u32   q9, q1              \n" // _src15ttt_r
                "vst2.u8    {d16-d17}, [%4], %11\n"

                "subs       %0, #1              \n"

                "vst2.u8    {d18-d19}, [%3], %11\n"
                "vst2.u8    {d4-d5}, [%4], %11  \n"
                "vst2.u8    {d6-d7}, [%3], %11  \n"
                "vst2.u8    {d0-d1}, [%4], %11  \n"
                "vst2.u8    {d2-d3}, [%3], %11  \n"

                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(src1), // %2
                "=r"(dst0), // %3
                "=r"(dst1)  // %4
                : "0"(nn),
                "1"(src0),
                "2"(src1),
                "3"(dst0),
                "4"(dst1),
                "r"(src_step), // %10
                "r"(dst_step)  // %11
                : "cc", "memory", "q0", "q1", "q2", "q3", "q8", "q9", "q10", "q11");
        }
#endif // __aarch64__
        for (; remain > 0; remain--) {
            dst0[0] = src1[0 + 3 * src_step];
            dst0[1] = src1[1 + 3 * src_step];
            dst0[2] = src0[0 + 3 * src_step];
            dst0[3] = src0[1 + 3 * src_step];
            dst0[4] = src1[0 + 2 * src_step];
            dst0[5] = src1[1 + 2 * src_step];
            dst0[6] = src0[0 + 2 * src_step];
            dst0[7] = src0[1 + 2 * src_step];
            dst0[8] = src1[0 + src_step];
            dst0[9] = src1[1 + src_step];
            dst0[10] = src0[0 + src_step];
            dst0[11] = src0[1 + src_step];
            dst0[12] = src1[0];
            dst0[13] = src1[1];
            dst0[14] = src0[0];
            dst0[15] = src0[1];

            src0 += 2;
            src1 += 2;

            dst0 += stride;
        }

        src0 += srcwgap + 7 * srcstride;
    }
#endif // __ARM_NEON
    for (; y < srch; y++) {
        unsigned char *dst0 = dstend - y * 2 - 2;

        int x = 0;
        for (; x < srcw; x++) {
            dst0[0] = src0[0];
            dst0[1] = src0[1];

            src0 += 2;
            dst0 += stride;
        }

        src0 += srcwgap;
    }
}


static void mirrorC2(const unsigned char* src, int srcw, int srch, int srcstride, unsigned char* dst, int w, int /*h*/, int stride)
{
    const int srcwgap = srcstride - srcw * 2;
    const int wgap = stride + w * 2;

    const unsigned char* src0 = src;
    unsigned char* dst0 = dst + w * 2 - 2;

    int y = 0;
    for (; y < srch; y++)
    {
#if __ARM_NEON
        dst0 -= 7 * 2;

        int nn = srcw >> 4;
        int remain = srcw - (nn << 4);

#if __aarch64__
        for (; nn > 0; nn--)
        {
            uint8x8x2_t _src = vld2_u8(src0);
            uint8x8x2_t _src2 = vld2_u8(src0 + 8 * 2);

            _src.val[0] = vrev64_u8(_src.val[0]);
            _src.val[1] = vrev64_u8(_src.val[1]);

            _src2.val[0] = vrev64_u8(_src2.val[0]);
            _src2.val[1] = vrev64_u8(_src2.val[1]);

            vst2_u8(dst0, _src);
            vst2_u8(dst0 - 8 * 2, _src2);

            src0 += 16 * 2;
            dst0 -= 16 * 2;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "mov        r4, #-16            \n"
                "0:                             \n"
                "pld        [%1, #128]          \n"
                "vld2.u8    {d0-d1}, [%1]!      \n"
                "vrev64.u8  d0, d0              \n"
                "pld        [%1, #128]          \n"
                "vld2.u8    {d2-d3}, [%1]!      \n"
                "vrev64.u8  d1, d1              \n"
                "vrev64.u8  d2, d2              \n"
                "vst2.u8    {d0-d1}, [%2], r4   \n"
                "vrev64.u8  d3, d3              \n"
                "subs       %0, #1              \n"
                "vst2.u8    {d2-d3}, [%2], r4   \n"
                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(dst0)  // %2
                : "0"(nn),
                "1"(src0),
                "2"(dst0)
                : "cc", "memory", "q0", "q1", "r4");
        }
#endif // __aarch64__

        dst0 += 7 * 2;
#else
        int remain = srcw;
#endif // __ARM_NEON

        for (; remain > 0; remain--)
        {
            dst0[0] = src0[0];
            dst0[1] = src0[1];

            src0 += 2;
            dst0 -= 2;
        }

        src0 += srcwgap;
        dst0 += wgap;
    }
}

void
ImageRotateHelper::mirrorC3(const unsigned char *src, int srcw, int srch, int srcstride,
                            unsigned char *dst,
                            int w, int /*h*/, int stride) {
    const int srcwgap = srcstride - srcw * 3;
    const int wgap = stride + w * 3;

    const unsigned char *src0 = src;
    unsigned char *dst0 = dst + w * 3 - 3;

    int y = 0;
    for (; y < srch; y++) {
#if __ARM_NEON
        dst0 -= 7 * 3;

        int nn = srcw >> 4;
        int remain = srcw - (nn << 4);

#if __aarch64__
        for (; nn > 0; nn--) {
            uint8x8x3_t _src = vld3_u8(src0);
            uint8x8x3_t _src2 = vld3_u8(src0 + 8 * 3);

            _src.val[0] = vrev64_u8(_src.val[0]);
            _src.val[1] = vrev64_u8(_src.val[1]);
            _src.val[2] = vrev64_u8(_src.val[2]);

            _src2.val[0] = vrev64_u8(_src2.val[0]);
            _src2.val[1] = vrev64_u8(_src2.val[1]);
            _src2.val[2] = vrev64_u8(_src2.val[2]);

            vst3_u8(dst0, _src);
            vst3_u8(dst0 - 8 * 3, _src2);

            src0 += 16 * 3;
            dst0 -= 16 * 3;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "mov        r4, #-24            \n"
                "0:                             \n"
                "pld        [%1, #192]          \n"
                "vld3.u8    {d0-d2}, [%1]!      \n"
                "vrev64.u8  d0, d0              \n"
                "vrev64.u8  d1, d1              \n"
                "pld        [%1, #192]          \n"
                "vld3.u8    {d4-d6}, [%1]!      \n"
                "vrev64.u8  d2, d2              \n"
                "vrev64.u8  d4, d4              \n"
                "vst3.u8    {d0-d2}, [%2], r4   \n"
                "vrev64.u8  d5, d5              \n"
                "vrev64.u8  d6, d6              \n"
                "subs       %0, #1              \n"
                "vst3.u8    {d4-d6}, [%2], r4   \n"
                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(dst0)  // %2
                : "0"(nn),
                "1"(src0),
                "2"(dst0)
                : "cc", "memory", "q0", "q1", "q2", "q3", "r4");
        }
#endif // __aarch64__

        dst0 += 7 * 3;
#else
        int remain = srcw;
#endif // __ARM_NEON

        for (; remain > 0; remain--) {
            dst0[0] = src0[0];
            dst0[1] = src0[1];
            dst0[2] = src0[2];

            src0 += 3;
            dst0 -= 3;
        }

        src0 += srcwgap;
        dst0 += wgap;
    }
}

static void rotate180C1(const unsigned char* src, int srcw, int srch, int srcstride, unsigned char* dst, int w, int h, int stride)
{
    const int srcwgap = srcstride - srcw;
    const int wgap = stride - w;

    // point to the last dst pixel
    unsigned char* dstend = dst + stride * h - wgap;

    const unsigned char* src0 = src;
    unsigned char* dst0 = dstend - 1;

    int y = 0;
    for (; y < srch; y++)
    {
#if __ARM_NEON
        dst0 -= 15;

        int nn = srcw >> 4;
        int remain = srcw - (nn << 4);

#if __aarch64__
        for (; nn > 0; nn--)
        {
            uint8x8_t _src = vld1_u8(src0);
            uint8x8_t _src2 = vld1_u8(src0 + 8);

            _src = vrev64_u8(_src);
            _src2 = vrev64_u8(_src2);

            vst1_u8(dst0, _src2);
            vst1_u8(dst0 + 8, _src);

            src0 += 16;
            dst0 -= 16;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "mov        r4, #-16            \n"
                "0:                             \n"
                "pld        [%1, #128]          \n"
                "vld1.u8    {d0-d1}, [%1]!      \n"
                "vrev64.u8  d3, d0              \n"
                "vrev64.u8  d2, d1              \n"
                "subs       %0, #1              \n"
                "vst1.u8    {d2-d3}, [%2], r4   \n"
                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(dst0)  // %2
                : "0"(nn),
                "1"(src0),
                "2"(dst0)
                : "cc", "memory", "q0", "q1", "r4");
        }
#endif // __aarch64__

        dst0 += 15;
#else
        int remain = srcw;
#endif // __ARM_NEON

        for (; remain > 0; remain--)
        {
            *dst0 = *src0;

            src0 += 1;
            dst0 -= 1;
        }

        src0 += srcwgap;
        dst0 -= wgap;
    }
}


static void rotate180C2(const unsigned char* src, int srcw, int srch, int srcstride, unsigned char* dst, int w, int h, int stride)
{
    const int srcwgap = srcstride - srcw * 2;
    const int wgap = stride - w * 2;

    // point to the last dst pixel
    unsigned char* dstend = dst + stride * h - wgap;

    const unsigned char* src0 = src;
    unsigned char* dst0 = dstend - 2;

    int y = 0;
    for (; y < srch; y++)
    {
#if __ARM_NEON
        dst0 -= 7 * 2;

        int nn = srcw >> 4;
        int remain = srcw - (nn << 4);

#if __aarch64__
        for (; nn > 0; nn--)
        {
            uint8x8x2_t _src = vld2_u8(src0);
            uint8x8x2_t _src2 = vld2_u8(src0 + 8 * 2);

            _src.val[0] = vrev64_u8(_src.val[0]);
            _src.val[1] = vrev64_u8(_src.val[1]);

            _src2.val[0] = vrev64_u8(_src2.val[0]);
            _src2.val[1] = vrev64_u8(_src2.val[1]);

            vst2_u8(dst0, _src);
            vst2_u8(dst0 - 8 * 2, _src2);

            src0 += 16 * 2;
            dst0 -= 16 * 2;
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "mov        r4, #-16            \n"
                "0:                             \n"
                "pld        [%1, #128]          \n"
                "vld2.u8    {d0-d1}, [%1]!      \n"
                "vrev64.u8  d0, d0              \n"
                "pld        [%1, #128]          \n"
                "vld2.u8    {d2-d3}, [%1]!      \n"
                "vrev64.u8  d1, d1              \n"
                "vrev64.u8  d2, d2              \n"
                "vst2.u8    {d0-d1}, [%2], r4   \n"
                "vrev64.u8  d3, d3              \n"
                "subs       %0, #1              \n"
                "vst2.u8    {d2-d3}, [%2], r4   \n"
                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(dst0)  // %2
                : "0"(nn),
                "1"(src0),
                "2"(dst0)
                : "cc", "memory", "q0", "q1", "r4");
        }
#endif // __aarch64__

        dst0 += 7 * 2;
#else
        int remain = srcw;
#endif // __ARM_NEON

        for (; remain > 0; remain--)
        {
            dst0[0] = src0[0];
            dst0[1] = src0[1];

            src0 += 2;
            dst0 -= 2;
        }

        src0 += srcwgap;
        dst0 -= wgap;
    }
}

void
ImageRotateHelper::mirrorC1(const unsigned char *src, int srcw, int srch, int srcstride,
                            unsigned char *dst,
                            int w, int /*h*/, int stride) {
    const int srcwgap = srcstride - srcw;
    const int wgap = stride + w;

    const unsigned char *src0 = src;
    unsigned char *dst0 = dst + w - 1;

    int y = 0;
    for (; y < srch; y++) {
#if __ARM_NEON
        dst0 -= 15;

        int nn = srcw >> 4;
        int remain = srcw - (nn << 4);

#if __aarch64__
        if (nn > 0) {
            asm volatile(
            "mov        x4, #-16            \n"
            "0:                             \n"
            "prfm  pldl1keep, [%1, #128]    \n"
            "ld1 {v0.16b}, [%1], #16          \n"
            "rev64  v1.16b, v0.16b          \n"
            "ext  v0.16b,v1.16b,v1.16b, #8  \n"
            "subs       %w0,%w0, #1             \n"
            "st1    {v0.16b}, [%2], x4        \n"
            "bne        0b                  \n"
            : "=r"(nn),   // %0
            "=r"(src0), // %1
            "=r"(dst0)  // %2
            : "0"(nn),
            "1"(src0),
            "2"(dst0)
            : "cc", "memory", "v0", "v1", "v2", "v3", "x4");
        }
#else
        if (nn > 0)
        {
            asm volatile(
                "mov        r4, #-16            \n"
                "0:                             \n"
                "pld        [%1, #128]          \n"
                "vld1.u8    {d0-d1}, [%1]!      \n"
                "vrev64.u8  d3, d0              \n"
                "vrev64.u8  d2, d1              \n"
                "subs       %0, #1              \n"
                "vst1.u8    {d2-d3}, [%2], r4   \n"
                "bne        0b                  \n"
                : "=r"(nn),   // %0
                "=r"(src0), // %1
                "=r"(dst0)  // %2
                : "0"(nn),
                "1"(src0),
                "2"(dst0)
                : "cc", "memory", "q0", "q1", "r4");
        }
#endif // __aarch64__

        dst0 += 15;
#else
        int remain = srcw;
#endif // __ARM_NEON

        for (; remain > 0; remain--) {
            *dst0 = *src0;

            src0 += 1;
            dst0 -= 1;
        }

        src0 += srcwgap;
        dst0 += wgap;
    }
}

void ImageRotateHelper::mirrorNv(const unsigned char *src, int srcw, int srch, unsigned char *dst,
                                    int w,
                                    int h) {
    // assert srcw % 2 == 0
    // assert srch % 2 == 0
    // assert w % 2 == 0
    // assert h % 2 == 0

    const unsigned char *srcY = src;
    unsigned char *dstY = dst;
    mirrorC1(srcY, srcw, srch, srcw, dstY, w, h, w);

    const unsigned char *srcUV = src + srcw * srch;
    unsigned char *dstUV = dst + w * h;
    mirrorC2(srcUV, srcw / 2, srch / 2, srcw, dstUV, w / 2, h / 2, w);
}



void ImageRotateHelper::rotateNv180(const unsigned char *src, int srcw, int srch, unsigned char *dst,
                                   int w,
                                   int h) {
    // assert srcw % 2 == 0
    // assert srch % 2 == 0
    // assert w % 2 == 0
    // assert h % 2 == 0

    const unsigned char *srcY = src;
    unsigned char *dstY = dst;
    rotate180C1(srcY, srcw, srch, srcw, dstY, w, h, w);

    const unsigned char *srcUV = src + srcw * srch;
    unsigned char *dstUV = dst + w * h;
    rotate180C2(srcUV, srcw / 2, srch / 2, srcw, dstUV, w / 2, h / 2, w);
}


void ImageRotateHelper::rotateNv270(const unsigned char *src, int srcw, int srch,
                                    unsigned char *dst, int w, int h) {
    // assert srcw % 2 == 0
    // assert srch % 2 == 0
    // assert w % 2 == 0
    // assert h % 2 == 0

    const unsigned char *srcY = src;
    unsigned char *dstY = dst;
    rotate270(srcY, srcw, srch, srcw, dstY, w, h, w);

    const unsigned char *srcUV = src + srcw * srch;
    unsigned char *dstUV = dst + w * h;
    rotate270c2(srcUV, srcw / 2, srch / 2, srcw, dstUV, w / 2, h / 2, w);
}


void ImageRotateHelper::rotateNv90(const unsigned char *src, int srcw, int srch, unsigned char *dst,
                                   int w,
                                   int h) {
    // assert srcw % 2 == 0
    // assert srch % 2 == 0
    // assert w % 2 == 0
    // assert h % 2 == 0

    const unsigned char *srcY = src;
    unsigned char *dstY = dst;
    rotate90C1(srcY, srcw, srch, srcw, dstY, w, h, w);

    const unsigned char *srcUV = src + srcw * srch;
    unsigned char *dstUV = dst + w * h;
    rotate90C2(srcUV, srcw / 2, srch / 2, srcw, dstUV, w / 2, h / 2, w);
}
