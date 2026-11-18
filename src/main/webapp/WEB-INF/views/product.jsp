<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:1040px;">
  <div class="mb-3">
    <a href="${pageContext.request.contextPath}/home" class="text-muted-soft small">&larr; Kembali ke Katalog</a>
  </div>

  <!-- Product Hero -->
  <div class="glass-card p-4 mb-4">
    <div class="row g-4 align-items-center">
      <div class="col-md-3 text-center">
        <div class="p-thumb mx-auto" style="height:170px; max-width:210px;">
          <img src="${pageContext.request.contextPath}${product.thumbnailImg}" alt="<c:out value='${product.namaProduk}'/>"
               onerror="this.src='${pageContext.request.contextPath}/static/Image/Logo/Logo.png'">
        </div>
      </div>
      <div class="col-md-9">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-2">
          <a href="${pageContext.request.contextPath}/home?provider=${product.idProvider}"
             class="badge text-bg-dark border border-secondary px-3 py-2">
            <c:out value="${product.providerNama}" />
          </a>
          <button type="button" class="btn btn-sm btn-ghost" id="copyShareBtn">Salin Tautan</button>
        </div>

        <h2 class="fw-bold text-white mb-2"><c:out value="${product.namaProduk}" /></h2>
        <p class="text-muted-soft mb-3"><c:out value="${product.deskripsi}" /></p>

        <div class="d-flex flex-wrap gap-2">
          <span class="badge rounded-pill" style="background:rgba(168,85,247,0.2); border:1px solid var(--border); color:#e9d5ff;">
            <c:out value="${product.jenisProduk}" />
          </span>
          <c:forEach var="tag" items="${product.tags}">
            <a href="${pageContext.request.contextPath}/home?tag=${tag.idTag}"
               class="badge rounded-pill text-decoration-none"
               style="background:rgba(255,255,255,0.08); border:1px solid rgba(255,255,255,0.18); color:#f5f1ff;">
              <c:out value="${tag.namaTag}" />
            </a>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>

  <!-- Detail Varian & Stok -->
  <div class="glass-card p-4">
    <div class="row align-items-center g-3">
      <div class="col-md-8">
        <h5 class="fw-bold text-white mb-1"><c:out value="${product.namaProduk}" /> (Pengiriman Instan)</h5>
        <div class="d-flex gap-3 flex-wrap small text-muted-soft mt-2">
          <span>
            Status Stok:
            <c:choose>
              <c:when test="${product.availableStock > 0}">
                <strong class="text-success">Tersedia (<c:out value="${product.availableStock}" />)</strong>
              </c:when>
              <c:otherwise>
                <strong class="text-danger">Stok Kosong</strong>
              </c:otherwise>
            </c:choose>
          </span>
          <span>Total Terjual: <strong class="text-white"><c:out value="${product.soldStock}" /></strong></span>
          <span>Status Produk: <strong class="text-white"><c:out value="${product.statusProduk}" /></strong></span>
        </div>
      </div>

      <div class="col-md-4 text-md-end">
        <div class="fs-4 fw-bold text-white mb-2">
          Rp <fmt:formatNumber value="${product.hargaProduk}" groupingUsed="true" />
        </div>
        <c:choose>
          <c:when test="${product.availableStock > 0 && product.statusProduk == 'Available'}">
            <a href="${pageContext.request.contextPath}/checkout?id=${product.idProduk}" class="btn btn-accent px-4 py-2">
              Beli Sekarang
            </a>
          </c:when>
          <c:otherwise>
            <button type="button" class="btn btn-secondary px-4 py-2" disabled>Stok Tidak Tersedia</button>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </div>
</div>

<script>
  document.getElementById('copyShareBtn')?.addEventListener('click', async function () {
    try {
      await navigator.clipboard.writeText(window.location.href);
      this.textContent = 'Tautan Disalin';
      setTimeout(() => { this.textContent = 'Salin Tautan'; }, 2000);
    } catch (e) {
      // Clipboard API unavailable
    }
  });
</script>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

