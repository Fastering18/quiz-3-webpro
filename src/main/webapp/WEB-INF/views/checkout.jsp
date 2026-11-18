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
      <img src="${pageContext.request.contextPath}${product.thumbnailImg}" alt="<c:out value='${product.namaProduk}'/>"
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

        <h5 class="fw-bold text-white mb-3">Informasi Pembeli</h5>

        <c:choose>
          <c:when test="${sessionScope.user != null}">
            <div class="p-3 rounded-3 mb-3" style="background:rgba(34,197,94,0.12); border:1px solid rgba(34,197,94,0.3);">
              <div class="fw-semibold text-white">Login sebagai <c:out value="${sessionScope.user.namaPelanggan}" /></div>
              <div class="small text-muted-soft">
                Email: <c:out value="${sessionScope.user.gmailPelanggan}" /> |
                Level: <c:out value="${sessionScope.user.namaLevel}" />
                <c:if test="${discountPercent > 0}">
                  (Diskon <c:out value="${discountPercent}" />%)
                </c:if>
              </div>
            </div>
          </c:when>
          <c:otherwise>
            <div class="mb-3">
              <label class="form-label fw-semibold">Nama Lengkap</label>
              <input type="text" name="buyer_name" class="form-control form-dark" required placeholder="Nama penerima lisensi">
            </div>
            <div class="mb-3">
              <label class="form-label fw-semibold">Email Penerima</label>
              <input type="email" name="buyer_email" class="form-control form-dark" required placeholder="nama@email.com">
            </div>
          </c:otherwise>
        </c:choose>

        <div class="mb-3" style="max-width:200px;">
          <label class="form-label fw-semibold">Jumlah (Stok: <c:out value="${product.availableStock}" />)</label>
          <input type="number" id="qtyInput" name="qty" class="form-control form-dark"
                 value="1" min="1" max="${sessionScope.user != null ? product.availableStock : 1}"
                 ${sessionScope.user == null ? 'readonly' : ''}>
          <c:if test="${sessionScope.user == null}">
            <div class="form-text text-muted-soft">Pembelian tamu dibatasi 1 item. Login untuk membeli lebih banyak.</div>
          </c:if>
        </div>

        <h5 class="fw-bold text-white mt-4 mb-3">Metode Pembayaran</h5>

        <div class="d-grid gap-2">
          <c:if test="${sessionScope.user != null}">
            <label class="p-3 rounded-3 d-flex align-items-center gap-3"
                   style="background:rgba(10,8,18,0.85); border:1px solid var(--border); cursor:pointer;">
              <input type="radio" name="method" value="saldo" id="radioSaldo"
                     ${sessionScope.user.saldo >= effectivePrice ? '' : 'disabled'}>
              <div>
                <div class="fw-semibold text-white">
                  Saldo Akun (Rp <fmt:formatNumber value="${sessionScope.user.saldo}" groupingUsed="true" />)
                </div>
                <div class="small text-muted-soft" id="saldoHint">
                  Pembayaran instan langsung selesai dan kunci lisensi langsung tampil.
                </div>
              </div>
            </label>
          </c:if>

          <label class="p-3 rounded-3 d-flex align-items-center gap-3"
                 style="background:rgba(10,8,18,0.85); border:1px solid var(--border); cursor:pointer;">
            <input type="radio" name="method" value="qris" checked>
            <div>
              <div class="fw-semibold text-white">
                QRIS via Sumopod
                <c:choose>
                  <c:when test="${sumopodConfigured}">
                    <span class="badge text-bg-info ms-1">${sumopodSandbox ? 'Sandbox' : 'Live'}</span>
                  </c:when>
                  <c:otherwise>
                    <span class="badge text-bg-warning ms-1">Simulasi Lokal</span>
                  </c:otherwise>
                </c:choose>
              </div>
              <div class="small text-muted-soft">
                <c:choose>
                  <c:when test="${sumopodConfigured}">
                    Membuat tautan pembayaran QRIS di Sumopod (<c:out value="${sumopodSandbox ? 'Sandbox' : 'Production'}" />).
                  </c:when>
                  <c:otherwise>
                    API key Sumopod belum diisi; pesanan dibuat sebagai tagihan pending lokal untuk uji coba.
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </label>

          <label class="p-3 rounded-3 d-flex align-items-center gap-3"
                 style="background:rgba(10,8,18,0.85); border:1px solid var(--border); cursor:pointer;">
            <input type="radio" name="method" value="bank">
            <div>
              <div class="fw-semibold text-white">Transfer Bank Virtual Account</div>
              <div class="small text-muted-soft">BCA, Mandiri, BNI, atau BRI Virtual Account.</div>
            </div>
          </label>
        </div>

        <div class="d-flex gap-2 mt-4">
          <button type="submit" class="btn btn-accent px-4 py-2"
                  ${product.availableStock <= 0 ? 'disabled' : ''}>
            Buat Pesanan
          </button>
          <a href="${pageContext.request.contextPath}/product?id=${product.idProduk}" class="btn btn-ghost px-4 py-2">Batal</a>
        </div>
      </form>
    </div>

    <!-- Order Summary -->
    <div class="col-lg-5">
      <div class="glass-card p-4" id="summaryBox"
           data-unit-price="${effectivePrice}"
           data-saldo="${sessionScope.user != null ? sessionScope.user.saldo : 0}">
        <h5 class="fw-bold text-white mb-3">Ringkasan Pesanan</h5>
        <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
          <span class="text-muted-soft">Produk</span>
          <span class="fw-semibold text-end"><c:out value="${product.namaProduk}" /></span>
        </div>
        <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
          <span class="text-muted-soft">Harga Normal</span>
          <span>Rp <fmt:formatNumber value="${product.hargaProduk}" groupingUsed="true" /></span>
        </div>
        <c:if test="${discountPercent > 0}">
          <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
            <span class="text-muted-soft">Diskon Level (<c:out value="${discountPercent}" />%)</span>
            <span class="text-success fw-semibold">
              Rp <fmt:formatNumber value="${effectivePrice}" groupingUsed="true" /> / item
            </span>
          </div>
        </c:if>
        <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
          <span class="text-muted-soft">Jumlah</span>
          <span id="sumQty">1</span>
        </div>
        <div class="d-flex justify-content-between py-3 fs-5 fw-bold text-white">
          <span>Total Pembayaran</span>
          <span id="sumTotal">Rp <fmt:formatNumber value="${effectivePrice}" groupingUsed="true" /></span>
        </div>
        <p class="small text-muted-soft mb-0">
          Kunci lisensi akan dialokasikan dari stok tersedia dan ditampilkan di halaman invoice setelah pembayaran selesai.
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
    const radioSaldo = document.getElementById('radioSaldo');
    const saldoHint = document.getElementById('saldoHint');
    if (!qtyInput || !summaryBox) return;

    const unitPrice = parseInt(summaryBox.dataset.unitPrice || '0', 10);
    const saldo = parseInt(summaryBox.dataset.saldo || '0', 10);
    const formatRp = n => 'Rp ' + new Intl.NumberFormat('id-ID').format(n);

    function updateSummary() {
      let q = parseInt(qtyInput.value || '1', 10);
      const max = parseInt(qtyInput.getAttribute('max') || '1', 10);
      if (isNaN(q) || q < 1) q = 1;
      if (q > max && max > 0) q = max;
      const total = unitPrice * q;
      sumQty.textContent = q;
      sumTotal.textContent = formatRp(total);

      if (radioSaldo) {
        const enough = saldo >= total;
        radioSaldo.disabled = !enough;
        if (!enough && radioSaldo.checked) {
          document.querySelector('input[name="method"][value="qris"]').checked = true;
        }
        if (saldoHint) {
          saldoHint.textContent = enough
            ? 'Saldo cukup. Pembayaran instan langsung selesai.'
            : 'Saldo tidak mencukupi untuk total ' + formatRp(total) + '.';
        }
      }
    }

    qtyInput.addEventListener('input', updateSummary);
    updateSummary();
  })();
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

