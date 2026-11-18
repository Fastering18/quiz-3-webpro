<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:860px;">
  <c:if test="${param.created == '1'}">
    <div class="alert alert-success">Pesanan berhasil dibuat. Silakan selesaikan pembayaran jika berstatus Unpaid.</div>
  </c:if>
  <c:if test="${param.paid == '1'}">
    <div class="alert alert-success">Pembayaran berhasil dikonfirmasi. Kunci lisensi Anda sudah aktif di bawah ini.</div>
  </c:if>
  <c:if test="${param.cancelled == '1'}">
    <div class="alert alert-warning">Pesanan telah dibatalkan dan stok lisensi telah dikembalikan.</div>
  </c:if>
  <c:if test="${not empty param.simulated}">
    <div class="alert alert-info">Simulasi webhook Sumopod dijalankan dengan status: <strong><c:out value="${param.simulated}" /></strong>.</div>
  </c:if>

  <c:choose>
    <c:when test="${trx == null}">
      <div class="glass-card p-4 p-md-5">
        <h3 class="fw-bold text-white mb-2">Cek Status Invoice</h3>
        <p class="text-muted-soft mb-4">Masukkan nomor ID Transaksi untuk melihat detail tagihan atau kunci lisensi Anda.</p>
        <c:if test="${not empty error}">
          <div class="alert alert-danger"><c:out value="${error}" /></div>
        </c:if>
        <form method="GET" action="${pageContext.request.contextPath}/invoice" class="d-flex gap-2">
          <input type="number" name="id" class="form-control form-dark" placeholder="Contoh: 1" min="1" required>
          <button type="submit" class="btn btn-accent px-4">Cari</button>
        </form>
      </div>
    </c:when>
    <c:otherwise>
      <div class="glass-card p-4 p-md-5" id="invoiceRoot"
           data-trx-id="${trx.idTrx}"
           data-status="${trx.status}">
        <div class="d-flex justify-content-between align-items-start flex-wrap gap-3 pb-3 mb-4 border-bottom border-secondary border-opacity-25">
          <div>
            <h3 class="fw-bold text-white mb-1">Invoice #<c:out value="${trx.idTrx}" /></h3>
            <div class="small text-muted-soft">
              Order ID: <strong class="text-white"><c:out value="${trx.invoice != null ? trx.invoice.externalId : '-'}" /></strong>
              <c:if test="${trx.invoice != null && not empty trx.invoice.provider}">
                | Provider: <strong class="text-white"><c:out value="${trx.invoice.provider}" /></strong>
              </c:if>
            </div>
            <c:if test="${trx.invoice != null && not empty trx.invoice.providerPaymentId}">
              <div class="small text-muted-soft">
                Gateway Payment ID: <code><c:out value="${trx.invoice.providerPaymentId}" /></code>
              </div>
            </c:if>
          </div>
          <div class="text-md-end">
            <span class="status-badge status-${trx.statusClass}"><c:out value="${trx.status}" /></span>
            <div class="small text-muted-soft mt-2">
              Dibuat: <fmt:formatDate value="${trx.tanggalTrx}" pattern="dd MMM yyyy HH:mm" />
            </div>
            <c:if test="${trx.invoice != null && trx.invoice.expiresAt != null && trx.status == 'Unpaid'}">
              <div class="small text-warning">
                Kedaluwarsa: <fmt:formatDate value="${trx.invoice.expiresAt}" pattern="dd MMM yyyy HH:mm" />
              </div>
            </c:if>
          </div>
        </div>

        <!-- Tautan Pembayaran Sumopod jika Pending -->
        <c:if test="${trx.status == 'Unpaid' && trx.invoice != null && trx.invoice.externalPaymentLink}">
          <div class="p-3 rounded-3 mb-4 d-flex justify-content-between align-items-center flex-wrap gap-3"
               style="background:rgba(168,85,247,0.12); border:1px solid var(--accent);">
            <div>
              <div class="fw-bold text-white">Pembayaran QRIS Sumopod Siap</div>
              <div class="small text-muted-soft">
                Buka tautan Sumopod untuk membayar via QRIS. Halaman ini memeriksa status secara berkala.
              </div>
            </div>
            <div class="d-flex gap-2">
              <a href="<c:out value='${trx.invoice.paymentLink}'/>" target="_blank" rel="noopener"
                 class="btn btn-accent px-4">
                Bayar QRIS Sumopod
              </a>
              <a href="${pageContext.request.contextPath}/invoice?id=${trx.idTrx}" class="btn btn-ghost">
                Muat Ulang
              </a>
            </div>
          </div>
        </c:if>

        <div class="mb-4">
          <h6 class="text-uppercase small fw-bold mb-2" style="color:var(--accent);">Detail Pembeli</h6>
          <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
            <span class="text-muted-soft">Nama Penerima</span>
            <span><c:out value="${not empty trx.namaPenerima ? trx.namaPenerima : (not empty trx.namaPelanggan ? trx.namaPelanggan : 'Guest')}" /></span>
          </div>
          <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
            <span class="text-muted-soft">Email Pengiriman</span>
            <span><c:out value="${trx.gmailTrx}" /></span>
          </div>
          <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-25">
            <span class="text-muted-soft">Metode Pembayaran</span>
            <span><c:out value="${trx.metodePembayaran}" /></span>
          </div>
        </div>

        <div class="mb-4">
          <h6 class="text-uppercase small fw-bold mb-2" style="color:var(--accent);">Item &amp; Kunci Lisensi</h6>
          <c:forEach var="d" items="${trx.details}">
            <div class="p-3 rounded-3 mb-2" style="background:rgba(10,8,18,0.75); border:1px solid var(--border);">
              <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                <div>
                  <div class="fw-bold text-white"><c:out value="${d.namaProduk}" /></div>
                  <div class="small text-muted-soft">Stock ID #<c:out value="${d.idStk}" /></div>
                </div>
                <div class="fw-bold">
                  Rp <fmt:formatNumber value="${d.hargaBeli}" groupingUsed="true" />
                </div>
              </div>
              <div class="mt-2">
                <c:choose>
                  <c:when test="${trx.status == 'Completed'}">
                    <div class="license-box d-inline-block">
                      <c:out value="${d.kodeLisensi}" />
                    </div>
                  </c:when>
                  <c:when test="${trx.status == 'Failed' || trx.status == 'Expired' || trx.status == 'Cancelled'}">
                    <span class="small text-danger">Pesanan <c:out value="${trx.status}" />. Stok lisensi telah dikembalikan ke katalog.</span>
                  </c:when>
                  <c:otherwise>
                    <span class="small text-warning">Kunci lisensi akan ditampilkan setelah pembayaran selesai.</span>
                  </c:otherwise>
                </c:choose>
              </div>
            </div>
          </c:forEach>
        </div>

        <c:if test="${trx.invoice != null && trx.invoice.fee > 0}">
          <div class="d-flex justify-content-between py-2 border-top border-secondary border-opacity-25 small">
            <span class="text-muted-soft">Subtotal Produk</span>
            <span>Rp <fmt:formatNumber value="${trx.invoice.netAmount}" groupingUsed="true" /></span>
          </div>
          <div class="d-flex justify-content-between py-2 small">
            <span class="text-muted-soft">Biaya Layanan Gateway</span>
            <span>Rp <fmt:formatNumber value="${trx.invoice.fee}" groupingUsed="true" /></span>
          </div>
        </c:if>

        <div class="d-flex justify-content-between align-items-center py-3 border-top border-bottom border-secondary border-opacity-25 mb-4">
          <span class="fs-5 fw-bold text-white">Total Pembayaran</span>
          <span class="fs-4 fw-bold text-white">
            Rp <fmt:formatNumber value="${trx.totalPembayaran}" groupingUsed="true" />
          </span>
        </div>

        <div class="d-flex flex-wrap gap-2 justify-content-between">
          <div class="d-flex gap-2 flex-wrap">
            <c:if test="${trx.status == 'Unpaid'}">
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="confirm">
                <button type="submit" class="btn btn-success fw-semibold">Konfirmasi Pembayaran</button>
              </form>
              <form method="POST" action="${pageContext.request.contextPath}/invoice"
                    onsubmit="return confirm('Batalkan pesanan ini dan kembalikan stok?');">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="cancel">
                <button type="submit" class="btn btn-outline-danger">Batalkan Pesanan</button>
              </form>
            </c:if>
          </div>
          <div class="d-flex gap-2">
            <button type="button" class="btn btn-accent" onclick="window.print()">Cetak Invoice</button>
            <a href="${pageContext.request.contextPath}/home" class="btn btn-ghost">Kembali ke Katalog</a>
          </div>
        </div>

        <!-- Debug / Sandbox Controls -->
        <c:if test="${debugMode && trx.status == 'Unpaid'}">
          <div class="mt-4 p-3 rounded-3" style="background:rgba(255,255,255,0.03); border:1px dashed var(--border);">
            <div class="fw-bold small text-white mb-1">Mode Debug / Sandbox Simulator</div>
            <p class="small text-muted-soft mb-2">
              Gunakan tombol ini untuk menguji alur webhook Sumopod secara lokal tanpa tunnel publik, atau kirim POST ke
              <code>${pageContext.request.contextPath}/webhooks/sumopod</code> dengan Order ID
              <code><c:out value="${trx.invoice != null ? trx.invoice.externalId : ''}" /></code>.
            </p>
            <div class="d-flex gap-2 flex-wrap">
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="simulate_paid">
                <button type="submit" class="btn btn-sm btn-outline-success">Simulasikan PAID</button>
              </form>
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="simulate_failed">
                <button type="submit" class="btn btn-sm btn-outline-danger">Simulasikan FAILED</button>
              </form>
              <form method="POST" action="${pageContext.request.contextPath}/invoice">
                <input type="hidden" name="id_trx" value="${trx.idTrx}">
                <input type="hidden" name="action" value="simulate_expired">
                <button type="submit" class="btn btn-sm btn-outline-warning">Simulasikan EXPIRED</button>
              </form>
            </div>
          </div>
        </c:if>
      </div>
    </c:otherwise>
  </c:choose>
</div>

<script>
  (function () {
    const root = document.getElementById('invoiceRoot');
    if (!root || root.dataset.status !== 'Unpaid') return;
    const trxId = root.dataset.trxId;
    const pollUrl = '${pageContext.request.contextPath}/invoice?id=' + encodeURIComponent(trxId) + '&format=json';
    const timer = setInterval(async () => {
      try {
        const res = await fetch(pollUrl, { headers: { 'Accept': 'application/json' } });
        if (!res.ok) return;
        const data = await res.json();
        if (data.status && data.status !== 'Unpaid') {
          clearInterval(timer);
          window.location.reload();
        }
      } catch (e) {
        // Ignore transient network errors during polling.
      }
    }, 5000);
  })();
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
