<%@ include file="/WEB-INF/views/fragments/header.jspf" %>

<div class="container py-4" style="max-width:1040px;">
  <div class="mb-3">
    <a href="${pageContext.request.contextPath}/home" class="text-muted-soft small">&larr; Kembali ke Katalog</a>
  </div>

  <%-- Product Hero Card --%>
  <div class="glass-card p-4 mb-4">
    <div class="row g-4 align-items-center">
      <div class="col-md-3 text-center">
        <div class="p-thumb mx-auto" style="height:170px; max-width:210px;">
          <img src="${pageContext.request.contextPath}${product.thumbnailImg}"
               alt="<c:out value='${product.namaProduk}'/>"
               onerror="this.src='${pageContext.request.contextPath}/static/Image/Logo/Logo.png'">
        </div>
      </div>
      <div class="col-md-9">
        <%-- Category badge --%>
        <a href="${pageContext.request.contextPath}/home?provider=${product.idProvider}"
           class="pd-cat-badge">
          <c:out value="${product.providerNama}" />
        </a>

        <%-- Product title --%>
        <h2 class="pd-title"><c:out value="${product.namaProduk}" /></h2>

        <%-- Description --%>
        <p class="pd-desc"><c:out value="${product.deskripsi}" /></p>

        <%-- Tags --%>
        <div class="d-flex flex-wrap gap-2" style="margin-top:14px;">
          <span class="pd-tag"><c:out value="${product.jenisProduk}" /></span>
          <c:forEach var="tag" items="${product.tags}">
            <a href="${pageContext.request.contextPath}/home?tag=${tag.idTag}"
               class="pd-tag text-decoration-none">
              <c:out value="${tag.namaTag}" />
            </a>
          </c:forEach>
        </div>
      </div>
    </div>
  </div>

  <%-- Purchase card --%>
  <div class="pd-buy-card">
    <div class="pd-buy-left">
      <div class="pd-buy-name"><c:out value="${product.namaProduk}" /></div>
      <div class="d-flex align-items-center flex-wrap gap-2 pd-meta-row" style="margin-top:6px;">
        <c:choose>
          <c:when test="${product.availableStock > 0}">
            <span>Stok: <span class="pd-stock-available"><c:out value="${product.availableStock}" /> tersedia</span></span>
          </c:when>
          <c:otherwise>
            <span class="pd-stock-empty">Stok: Habis</span>
          </c:otherwise>
        </c:choose>
        <span class="pd-meta-sep">&middot;</span>
        <span>Terjual: <span class="pd-sold-count"><c:out value="${product.soldStock}" /></span></span>
      </div>
    </div>
    <div class="pd-buy-right">
      <div class="pd-price">Rp <fmt:formatNumber value="${product.hargaProduk}" groupingUsed="true" /></div>
      <c:choose>
        <c:when test="${product.availableStock > 0 && product.statusProduk == 'Available'}">
          <a href="${pageContext.request.contextPath}/checkout?id=${product.idProduk}"
             class="pd-btn-buy">Beli Sekarang</a>
        </c:when>
        <c:otherwise>
          <button type="button" class="pd-btn-disabled" disabled>Stok Tidak Tersedia</button>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
