<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4">
  <!-- Hero Banner -->
  <section class="hero-banner mb-4">
    <img src="${pageContext.request.contextPath}/static/Image/Background/Background-1.png"
         alt="Z-Store Banner" class="w-100">
  </section>

  <!-- Kategori / Provider -->
  <section id="kategori" class="mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="text-white fw-bold section-heading mb-0">Kategori</h4>
      <a href="${pageContext.request.contextPath}/home"
         class="cat-all-link ${activeProviderId == 0 ? 'is-active' : ''}">
        Semua Kategori
      </a>
    </div>

    <div class="row g-3">
      <c:forEach var="cat" items="${categories}">
        <div class="col-6 col-md-3">
          <a href="${pageContext.request.contextPath}/home?provider=${cat.idProvider}" class="d-block h-100">
            <div class="cat-card h-100 ${activeProviderId == cat.idProvider ? 'active' : ''}">
              <div class="cat-icon-wrap">
                <img src="${pageContext.request.contextPath}${cat.thumbnailImg}" alt="<c:out value='${cat.nama}'/>"
                     onerror="this.src='${pageContext.request.contextPath}/static/Image/Logo/Logo.png'">
              </div>
              <div class="text-white fw-semibold small"><c:out value="${cat.nama}" /></div>
              <div class="text-muted-soft" style="font-size:0.72rem;"><c:out value="${cat.productCount}" /> produk</div>
            </div>
          </a>
        </div>
      </c:forEach>
    </div>
  </section>

  <!-- Katalog Produk -->
  <section id="produk" class="mt-5">
    <!-- Tag filter — sits just above the section header -->
    <c:if test="${not empty tags}">
      <div class="d-flex align-items-center gap-2 flex-wrap pills mb-4">
        <span class="small text-muted-soft fw-semibold me-1" style="white-space:nowrap;">Filter Tag:</span>
        <a class="pill py-1 px-3 ${activeTagId == 0 ? 'active' : ''}"
           href="${pageContext.request.contextPath}/home${activeProviderId > 0 ? '?provider='.concat(activeProviderId) : ''}">
          Semua
        </a>
        <c:forEach var="t" items="${tags}">
          <a class="pill py-1 px-3 ${activeTagId == t.idTag ? 'active' : ''}"
             href="${pageContext.request.contextPath}/home?tag=${t.idTag}">
            <c:out value="${t.namaTag}" />
          </a>
        </c:forEach>
      </div>
    </c:if>

    <div class="d-flex justify-content-between align-items-end mb-3 flex-wrap gap-2 mt-4">
      <h4 class="text-white fw-bold section-heading mb-0">
        <c:choose>
          <c:when test="${not empty searchQuery}">Hasil Pencarian: "<c:out value="${searchQuery}" />"</c:when>
          <c:when test="${not empty activeCategoryName}"><c:out value="${activeCategoryName}" /></c:when>
          <c:otherwise>Katalog Produk Digital</c:otherwise>
        </c:choose>
      </h4>
      <small class="text-muted-soft"><c:out value="${products.size()}" /> produk ditemukan</small>
    </div>

    <div class="row g-3">
      <c:choose>
        <c:when test="${not empty products}">
          <c:forEach var="p" items="${products}">
            <div class="col-6 col-md-3">
              <a href="${pageContext.request.contextPath}/product?id=${p.idProduk}" class="d-block h-100">
                <div class="p-card h-100">
                  <c:choose>
                    <c:when test="${p.availableStock > 0}">
                      <span class="p-badge-stock">Stok: <c:out value="${p.availableStock}" /></span>
                    </c:when>
                    <c:otherwise>
                      <span class="p-badge-stock empty">Habis</span>
                    </c:otherwise>
                  </c:choose>
                  <div class="p-thumb">
                    <img src="${pageContext.request.contextPath}${p.thumbnailImg}" alt="<c:out value='${p.namaProduk}'/>"
                         onerror="this.src='${pageContext.request.contextPath}/static/Image/Logo/Logo.png'">
                  </div>
                  <div class="p-body">
                    <div>
                      <div class="p-meta"><c:out value="${p.providerNama}" /></div>
                      <div class="p-title"><c:out value="${p.namaProduk}" /></div>
                    </div>
                    <div class="p-price">
                      Rp <fmt:formatNumber value="${p.hargaProduk}" groupingUsed="true" />
                    </div>
                  </div>
                </div>
              </a>
            </div>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <div class="col-12">
            <div class="glass-card p-4 text-center">
              <p class="text-muted-soft mb-2">Tidak ada produk yang sesuai dengan filter.</p>
              <a href="${pageContext.request.contextPath}/home" class="btn btn-sm btn-accent">Lihat Semua Produk</a>
            </div>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </section>

  <!-- CTA Daftar Pelanggan -->
  <c:if test="${sessionScope.user == null}">
    <section class="mt-5">
      <div class="glass-card p-4 p-md-5">
        <div class="row align-items-center g-4">
          <div class="col-lg-8">
            <h3 class="fw-bold text-white mb-2">Daftar akun untuk kemudahan transaksi</h3>
            <p class="text-muted-soft mb-3">
              Kelola riwayat pembelian lisensi, top-up saldo akun untuk checkout instan, dan dapatkan diskon khusus level Reseller atau VIP.
            </p>
            <a href="${pageContext.request.contextPath}/register" class="btn btn-accent px-4 py-2">Daftar Sekarang</a>
          </div>
          <div class="col-lg-4 text-center">
            <img src="${pageContext.request.contextPath}/static/Image/Logo/Logo.png" alt="Z-Store" style="max-height:110px;">
          </div>
        </div>
      </div>
    </section>
  </c:if>
</div>



<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

