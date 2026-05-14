package com.rent.dao;

import com.rent.model.Tenant;
import com.rent.util.AuditActions;
import com.rent.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.rent.model.MoveOutSettlement;
import java.sql.ResultSet;

public class MoveOutSettlementDAO {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static boolean saveSettlement(
            Tenant tenant,
            String moveOutDate,
            double unpaidDue,
            double securityDeposit,
            double refundAmount,
            double payableAmount,
            String result,
            String reason
    ) {
        String sql = """
                INSERT INTO move_out_settlements
                (tenant_id, tenant_name, tenant_phone, flat_no,
                 move_out_date, unpaid_due, security_deposit,
                 refund_amount, payable_amount, result, reason, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenant.getId());
            ps.setString(2, tenant.getName());
            ps.setString(3, tenant.getPhone());
            ps.setString(4, tenant.getFlatNo());
            ps.setString(5, moveOutDate);
            ps.setDouble(6, unpaidDue);
            ps.setDouble(7, securityDeposit);
            ps.setDouble(8, refundAmount);
            ps.setDouble(9, payableAmount);
            ps.setString(10, result);
            ps.setString(11, reason);
            ps.setString(12, LocalDateTime.now().format(TS));

            int rows = ps.executeUpdate();

            if (rows > 0) {
                AuditLogDAO.log(
                        AuditActions.SETTLEMENT_CREATED,
                        "Move-out settlement created. Tenant: "
                                + tenant.getName()
                                + ", Flat: "
                                + tenant.getFlatNo()
                                + ", Unpaid Due: "
                                + unpaidDue
                                + ", Security Deposit: "
                                + securityDeposit
                                + ", Refund: "
                                + refundAmount
                                + ", Payable: "
                                + payableAmount
                                + ", Result: "
                                + result
                );
            }

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static MoveOutSettlement getLatestSettlementByTenantId(int tenantId) {
        String sql = """
            SELECT *
            FROM move_out_settlements
            WHERE tenant_id = ?
            ORDER BY id DESC
            LIMIT 1
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tenantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MoveOutSettlement s = new MoveOutSettlement();

                    s.setId(rs.getInt("id"));
                    s.setTenantId(rs.getInt("tenant_id"));
                    s.setTenantName(rs.getString("tenant_name"));
                    s.setTenantPhone(rs.getString("tenant_phone"));
                    s.setFlatNo(rs.getString("flat_no"));
                    s.setMoveOutDate(rs.getString("move_out_date"));
                    s.setUnpaidDue(rs.getDouble("unpaid_due"));
                    s.setSecurityDeposit(rs.getDouble("security_deposit"));
                    s.setRefundAmount(rs.getDouble("refund_amount"));
                    s.setPayableAmount(rs.getDouble("payable_amount"));
                    s.setResult(rs.getString("result"));
                    s.setReason(rs.getString("reason"));
                    s.setCreatedAt(rs.getString("created_at"));

                    return s;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean markSettlementAsSettled(
            int settlementId,
            double refundAmount,
            double payableAmount,
            String note
    ) {
        String sql = """
            UPDATE move_out_settlements
            SET refund_amount = ?,
                payable_amount = ?,
                result = 'SETTLED',
                reason = CASE
                    WHEN reason IS NULL OR reason = '' THEN ?
                    ELSE reason || CHAR(10) || ?
                END
            WHERE id = ?
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, refundAmount);
            ps.setDouble(2, payableAmount);
            ps.setString(3, note);
            ps.setString(4, note);
            ps.setInt(5, settlementId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}