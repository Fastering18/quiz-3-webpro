<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:1040px;">
  <div class="mb-3">
    <a href="${pageContext.request.contextPath}/product?id=${product.idProduk}" class="text-muted-soft small">&larr; Kembali ke Detail Produk</a>
  </div>

  <c:if test="${not empty error}">
    <div class="alert alert-danger"><c:out value="${error}" /></div>
  </c:if>

  <!-- Product Summary Hero -->
  <div class="glass-card p-3 mb-4 d-flex align-items-center gap-3 flex-wrap">
    <div class="p-thumb" style="width:88px; height:88px; flex-shrink:0;">
      <img src="<c:out value='${product.thumbnailSrc(pageContext.request.contextPath)}'/>"
           alt="<c:out value='${product.namaProduk}'/>"
           onerror="this.src='${pageContext.request.contextPath}/static/Image/Logo/Logo.png'">
    </div>
    <div>
      <span class="badge text-bg-dark border border-secondary mb-1"><c:out value="${product.providerNama}" /></span>
      <h4 class="fw-bold text-white mb-1"><c:out value="${product.namaProduk}" /></h4>
      <div class="small text-muted-soft"><c:out value="${product.deskripsi}" /></div>
    </div>
  </div>

  <div class="row g-4">
    <div class="col-lg-7">
      <form id="checkoutForm" class="glass-card p-4" method="POST" action="${pageContext.request.contextPath}/checkout">
        <input type="hidden" name="id_produk" value="${product.idProduk}">

        <h5 class="fw-semibold text-white mb-3">Informasi Pembeli</h5>

        <c:choose>
          <c:when test="${sessionScope.user != null}">
            <div class="buyer-info-card d-flex justify-content-between align-items-center flex-wrap gap-2">
              <div>
                <div class="fw-semibold text-white" style="font-size:0.925rem;">
                  <c:out value="${sessionScope.user.namaPelanggan}" />
                </div>
                <div style="font-size:0.80rem; color:#94a3b8; margin-top:2px;">
                  <c:out value="${sessionScope.user.gmailPelanggan}" />
                </div>
              </div>
              <c:if test="${discountPercent > 0}">
                <span class="buyer-tier-badge">
                  <c:out value="${sessionScope.user.namaLevel}" /> &middot; Diskon <c:out value="${discountPercent}" />%
                </span>
              </c:if>
              <c:if test="${discountPercent == 0}">
                <span class="buyer-tier-badge"><c:out value="${sessionScope.user.namaLevel}" /></span>
              </c:if>
            </div>
          </c:when>
          <c:otherwise>
            <div class="mb-3">
              <label class="form-label small fw-semibold">Nama Lengkap</label>
              <input type="text" name="buyer_name" class="form-control form-dark" required placeholder="Nama penerima lisensi">
            </div>
            <div class="mb-3">
              <label class="form-label small fw-semibold">Email Penerima</label>
              <input type="email" name="buyer_email" class="form-control form-dark" required placeholder="nama@email.com">
            </div>
          </c:otherwise>
        </c:choose>

        <div class="mb-4" style="max-width:220px;">
          <div class="d-flex align-items-baseline gap-2 mb-1">
            <label class="form-label small fw-semibold mb-0">Jumlah Pembelian</label>
            <span style="font-size:0.75rem; color:#94a3b8;">Tersedia <c:out value="${product.availableStock}" /> unit</span>
          </div>
          <input type="number" id="qtyInput" name="qty" class="form-control form-dark"
                 value="1" min="1" max="${sessionScope.user != null ? product.availableStock : 1}"
                 ${sessionScope.user == null ? 'readonly' : ''}>
          <c:if test="${sessionScope.user == null}">
            <div class="form-text text-muted-soft" style="font-size:0.75rem;">Login untuk membeli lebih dari 1 unit.</div>
          </c:if>
        </div>

        <h5 class="fw-semibold text-white mb-3">Metode Pembayaran</h5>

        <div class="d-grid gap-2">
          <label class="pay-method-label">
            <input type="radio" name="method" value="qris" checked>
            <div>
              <div class="pay-method-name">
                QRIS via Sumopod
                <c:if test="${(debugMode && !sumopodConfigured) || (sumopodConfigured && sumopodSandbox)}">
                  <span class="badge-sandbox">Sandbox</span>
                </c:if>
              </div>
              <div class="pay-method-desc">Pembayaran instan via scan QRIS Sumopod</div>
            </div>
          </label>
        </div>

        <div class="d-flex gap-2 mt-4">
          <button type="submit" class="btn btn-dash-primary px-4 py-2"
                  ${product.availableStock <= 0 ? 'disabled' : ''}>
            Buat Pesanan
          </button>
          <a href="${pageContext.request.contextPath}/product?id=${product.idProduk}"
             class="btn btn-cancel-neutral px-4 py-2">Batal</a>
        </div>
      </form>
    </div>

    <!-- Order Summary -->
    <div class="col-lg-5">
      <div class="glass-card p-4" id="summaryBox"
           data-unit-price="${effectivePrice}">
        <h5 class="fw-semibold text-white mb-3">Ringkasan Pesanan</h5>

        <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
          <span class="text-muted-soft small">Produk</span>
          <span class="fw-semibold text-end small" style="max-width:60%;"><c:out value="${product.namaProduk}" /></span>
        </div>
        <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
          <span class="text-muted-soft small">Harga Normal</span>
          <span class="small num-tabular">Rp <fmt:formatNumber value="${product.hargaProduk}" groupingUsed="true" /></span>
        </div>
        <c:if test="${discountPercent > 0}">
          <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
            <span class="text-muted-soft small">Diskon <c:out value="${discountPercent}" />%</span>
            <span class="small num-tabular" style="color:var(--green);">
              &minus; Rp <fmt:formatNumber value="${product.hargaProduk - effectivePrice}" groupingUsed="true" />
            </span>
          </div>
        </c:if>
        <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
          <span class="text-muted-soft small">Jumlah</span>
          <span class="small" id="sumQty">1</span>
        </div>
        <div class="d-flex justify-content-between align-items-center py-3">
          <span class="fw-semibold text-white">Total</span>
          <span class="fs-5 fw-bold text-white num-tabular" id="sumTotal">Rp <fmt:formatNumber value="${effectivePrice}" groupingUsed="true" /></span>
        </div>

        <p class="small text-muted-soft mb-0 mt-1" style="border-top:1px solid rgba(255,255,255,0.07); padding-top:12px;">
          Kunci lisensi langsung tampil di halaman invoice setelah pembayaran QRIS lunas.
        </p>
      </div>
    </div>
  </div>
</div>

<script>
  (function () {
    const qtyInput = document.getElementById('qtyInput');
    const summaryBox = document.getElementById('summaryBox');
    const sumQty = document.getElementById('sumQty');
    const sumTotal = document.getElementById('sumTotal');
    if (!qtyInput || !summaryBox) return;

    const unitPrice = parseInt(summaryBox.dataset.unitPrice || '0', 10);
    const formatRp = n => 'Rp ' + new Intl.NumberFormat('id-ID').format(n);

    function updateSummary() {
      let q = parseInt(qtyInput.value || '1', 10);
      const max = parseInt(qtyInput.getAttribute('max') || '1', 10);
      if (isNaN(q) || q < 1) q = 1;
      if (q > max && max > 0) q = max;
      const total = unitPrice * q;
      sumQty.textContent = q;
      sumTotal.textContent = formatRp(total);
    }

    qtyInput.addEventListener('input', updateSummary);
    updateSummary();
  })();
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
