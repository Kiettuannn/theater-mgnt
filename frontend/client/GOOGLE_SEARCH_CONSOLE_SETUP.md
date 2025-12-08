# 🔍 Google Search Console Setup - Step by Step

**Mục đích:** Để Google index website và tìm thấy trên tìm kiếm

**Time:** 15 phút

---

## STEP 1: Tạo Tài Khoản Google Search Console (2 phút)

### 1.1. Truy cập:
```
https://search.google.com/search-console
```

### 1.2. Đăng nhập:
- Dùng Gmail account
- Click "**Start now**"

---

## STEP 2: Add Property (3 phút)

### 2.1. Click "Add Property"

Chọn: **URL prefix**

### 2.2. Nhập URL:
```
https://theater-mgnt-seo.vercel.app
```

**IMPORTANT:**
- Phải có `https://`
- KHÔNG có `/` ở cuối
- Chính xác domain

Click "**Continue**"

---

## STEP 3: Verify Ownership (5 phút)

### 3.1. Chọn Verification Method:

Google sẽ hiện nhiều options, chọn: **HTML tag** (dễ nhất)

### 3.2. Copy Meta Tag:

Google sẽ cho bạn meta tag như này:

```html
<meta name="google-site-verification" content="ABC123XYZ..." />
```

**Copy phần: `ABC123XYZ...`** (content value only)

### 3.3. Update Code:

```bash
# Mở file layout.tsx
code C:\Users\pupgk\theater-mgnt\frontend\client\app\layout.tsx
```

Tìm dòng 66 (verification section):

```tsx
// BEFORE:
verification: {
  google: "your-google-site-verification-code",
},

// AFTER (paste code của bạn):
verification: {
  google: "ABC123XYZ...",  // ← Paste verification code ở đây
},
```

### 3.4. Save & Deploy:

```bash
cd C:\Users\pupgk\theater-mgnt

git add frontend/client/app/layout.tsx
git commit -m "feat: Add Google Search Console verification"
git push origin main

# Wait 2-3 minutes for Vercel to deploy
```

### 3.5. Verify Website:

Quay lại Google Search Console, click "**Verify**"

**Nếu thành công:** ✅ "Ownership verified"

**Nếu thất bại:**
- Đợi thêm 2-3 phút (Vercel deploy chưa xong)
- Check code đã đúng chưa (view page source)
- Try verify lại

---

## STEP 4: Submit Sitemap (2 phút)

### 4.1. Trong Google Search Console:

Left sidebar > Click "**Sitemaps**"

### 4.2. Add New Sitemap:

```
Sitemap URL: sitemap.xml
```

Click "**Submit**"

### 4.3. Expected Result:

```
Status: Success ✅
URLs discovered: 10+ (depends on movies)
```

**Note:** Có thể mất vài giờ để Google crawl sitemap.

---

## STEP 5: Request Indexing (3 phút)

### 5.1. URL Inspection:

Top bar > Nhập URL:
```
https://theater-mgnt-seo.vercel.app
```

Press Enter

### 5.2. Wait for Inspection:

Google sẽ check URL (~10 seconds)

### 5.3. Request Indexing:

Click "**Request Indexing**"

Google sẽ confirm: "Indexing requested"

### 5.4. Repeat for Important Pages:

Request indexing cho:
```
https://theater-mgnt-seo.vercel.app/
https://theater-mgnt-seo.vercel.app/movies/1
https://theater-mgnt-seo.vercel.app/movies/2
https://theater-mgnt-seo.vercel.app/booking
```

**Limit:** 10-15 requests/day (free account)

---

## STEP 6: Monitor Progress

### 6.1. Overview Dashboard:

Check: https://search.google.com/search-console

**Metrics to watch:**
- Total clicks
- Total impressions
- Average position
- CTR (Click-through rate)

### 6.2. Coverage Report:

Left sidebar > "**Coverage**"

Shows:
- Valid pages (indexed)
- Excluded pages
- Errors

**Target:** All important pages = "Valid"

### 6.3. Performance:

Left sidebar > "**Performance**"

Shows:
- Which keywords drive traffic
- Which pages get clicks
- Position in search results

**Note:** Data starts showing after 2-3 days

---

## EXPECTED TIMELINE:

```
Day 0 (Today):
  ✅ Setup Search Console
  ✅ Submit sitemap
  ✅ Request indexing

Day 1-2:
  📊 Google starts crawling
  📊 Coverage report shows "Discovered"

Day 3-7:
  ✅ Pages start getting indexed
  ✅ "Valid" count increases

Day 7-14:
  🎯 Website starts appearing in search
  🎯 Performance data available

Week 2-4:
  📈 Rankings improve
  📈 More keywords discovered
```

---

## TIPS FOR FASTER INDEXING:

### 1. Create Backlinks:

Share website links:
- Facebook/Twitter posts
- Reddit (relevant subreddits)
- LinkedIn
- GitHub README
- Personal blog

**Why:** Google discovers sites through links

### 2. Quality Content:

Add more:
- Unique movie descriptions
- Blog posts about movies
- Movie reviews
- News section

### 3. Fresh Content:

Update regularly:
- New movies
- New showtimes
- Update descriptions

**Why:** Google loves fresh content

### 4. Internal Linking:

Link between pages:
- Homepage → Movie pages
- Movie pages → Related movies
- Breadcrumbs

**Already have:** ✅ Good internal structure

### 5. Social Signals:

Share on:
- Facebook groups
- Movie forums
- Twitter hashtags: #phim #cinema #vietnam

---

## TROUBLESHOOTING:

### "URL not on Google"

**Reason:** Normal for new sites

**Fix:**
- Wait 3-7 days
- Request indexing again
- Check Coverage report

### "Submitted URL not found (404)"

**Reason:** URL doesn't exist or redirects

**Fix:**
- Check URL loads in browser
- Check sitemap URLs correct

### "Submitted URL has crawl issue"

**Reason:** Technical issue

**Fix:**
- Check robots.txt allows crawling
- Check no noindex tags
- Verify sitemap URLs

### "Coverage: Excluded"

**Reason:** Page intentionally not indexed

**Common reasons:**
- Duplicate content
- Low quality
- Noindex tag
- Robots.txt block

**Check:**
```
https://theater-mgnt-seo.vercel.app/robots.txt
```

Should allow all public pages.

---

## CHECKING CURRENT INDEX STATUS:

### Manual Check:

Google search:
```
site:theater-mgnt-seo.vercel.app
```

**Results:**
- 0 results = Not indexed yet
- 1+ results = Indexed! 🎉

### Specific Page:

```
site:theater-mgnt-seo.vercel.app/movies/1
```

---

## AFTER SETUP CHECKLIST:

- [ ] Google Search Console property added
- [ ] Website ownership verified
- [ ] Sitemap submitted
- [ ] Homepage indexing requested
- [ ] 3-5 movie pages indexing requested
- [ ] Verification meta tag in code
- [ ] Shared website link on social media
- [ ] Bookmarked Search Console dashboard

---

## MONITORING:

### Daily (first week):

```
✓ Check Coverage report
✓ Check indexing status: site:your-domain.com
✓ Request indexing for new pages
```

### Weekly:

```
✓ Review Performance data
✓ Check which keywords work
✓ Add new content
✓ Share more backlinks
```

### Monthly:

```
✓ Analyze trends
✓ Improve low-performing pages
✓ Update content
✓ Fix any errors
```

---

## REALISTIC EXPECTATIONS:

### Week 1:
- ✅ Setup complete
- 📊 Google discovers site
- ⏳ Indexing in progress

### Week 2-3:
- ✅ Homepage indexed
- ✅ Main pages indexed
- 📊 Performance data starts

### Month 1-2:
- 🎯 10-50 impressions/day
- 🎯 1-5 clicks/day
- 🎯 Ranking for brand name

### Month 3-6:
- 📈 100-500 impressions/day
- 📈 10-30 clicks/day
- 📈 Ranking for movie keywords

**Important:** SEO takes time! Don't expect overnight results.

---

## ADVANCED: Improve Rankings

### 1. Target Long-tail Keywords:

Instead of: "phim" (too competitive)

Target:
- "đặt vé xem phim online"
- "rạp chiếu phim giá rẻ"
- "xem phim mới nhất 2025"

### 2. Optimize Page Titles:

```tsx
// Current:
title: "Movie Name - Đặt vé xem phim"

// Better:
title: "Movie Name - Đặt vé online giá rẻ | Theater Management"
```

### 3. Add FAQ Schema:

Add FAQ structured data for movie pages.

### 4. Improve Page Speed:

- Optimize images (already have guide)
- Enable compression
- Minimize JavaScript

### 5. Mobile Optimization:

- Already responsive ✅
- Test: Google Mobile-Friendly Test

---

## USEFUL LINKS:

- **Search Console:** https://search.google.com/search-console
- **Index Check:** https://google.com (search: site:your-domain.com)
- **Mobile Test:** https://search.google.com/test/mobile-friendly
- **Rich Results:** https://search.google.com/test/rich-results
- **PageSpeed:** https://pagespeed.web.dev/

---

**Status:** Ready to setup! ✅

**Next:** Follow STEP 1-6 above
**Time:** 15 minutes
**Result:** Google will start indexing your site

Good luck! 🚀
